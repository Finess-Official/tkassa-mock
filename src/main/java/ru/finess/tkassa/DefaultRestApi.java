package ru.finess.tkassa;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import ru.finess.openapi.api.DefaultApiDelegate;
import ru.finess.openapi.api.DefaultApiResponses;
import ru.finess.openapi.model.GetState200Response;
import ru.finess.openapi.model.GetStateFULL;
import ru.finess.openapi.model.InitFULL;
import ru.finess.openapi.model.Response;
import ru.finess.tkassa.model.New;
import ru.finess.tkassa.model.PaymentState;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;

@Component
public class DefaultRestApi implements DefaultApiDelegate {

  private static final Function<PaymentState<?, ?>, PaymentState<?, ?>> HAPPY_PATH_TRANSITION =
      state -> (PaymentState<?, ?>) state.toOkState();
  private static final Function<PaymentState<?, ?>, PaymentState<?, ?>> ERROR_TRANSITION =
      state -> (PaymentState<?, ?>) state.toErrorState();

  private static final List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> HAPPY_PATH =
      List.of(
          HAPPY_PATH_TRANSITION,
          HAPPY_PATH_TRANSITION,
          HAPPY_PATH_TRANSITION,
          HAPPY_PATH_TRANSITION);

  private static final List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> REJECTED_PATH =
      List.of(HAPPY_PATH_TRANSITION, ERROR_TRANSITION);

  private static final List<Function<PaymentState<?, ?>, PaymentState<?, ?>>>
      DEADLINE_EXPIRED_PATH = List.of(ERROR_TRANSITION);

  private final ConcurrentHashMap<String, State> paymentStates = new ConcurrentHashMap<>();
  private final AtomicInteger paymentIdSequence = new AtomicInteger(0);
  private final MockConfiguration mockConfiguration;

  public DefaultRestApi(MockConfiguration mockConfiguration) {
    this.mockConfiguration = mockConfiguration;
  }

  record State(
      PaymentState<?, ?> currentState,
      List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> stateTransitions,
      int stateTransitionIndex) {

    public boolean isFinal() {
      return stateTransitionIndex >= stateTransitions.size();
    }

    public State nextState() {
      Function<PaymentState<?, ?>, PaymentState<?, ?>> transition =
          stateTransitions.get(stateTransitionIndex);
      return new State(transition.apply(currentState), stateTransitions, stateTransitionIndex + 1);
    }
  }

  @Override
  public DefaultApiResponses.GetStateApiResponse getState(GetStateFULL getStateFULL)
      throws Exception {
    String paymentId = getStateFULL.paymentId();
    State state = paymentStates.get(paymentId);
    if (state == null) {
      throw HttpServerResponseException.of(404, "Payment not found");
    }

    PaymentState<?, ?> currentState = state.currentState;
    if (!state.isFinal()) {
      state = state.nextState();
      paymentStates.put(paymentId, state);
    }

    GetState200Response response = currentState.toGetStateResponse();
    return new DefaultApiResponses.GetStateApiResponse(response);
  }

  @Override
  public DefaultApiResponses.InitApiResponse init(InitFULL initFULL) throws Exception {
    String paymentId = String.valueOf(paymentIdSequence.incrementAndGet());
    New paymentState =
        new New(initFULL.terminalKey(), initFULL.amount(), initFULL.orderId(), paymentId);

    List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> path =
        switch (mockConfiguration.paymentTransitionStrategy()) {
          case HAPPY_PATH -> HAPPY_PATH;
          case REJECTED_PATH -> REJECTED_PATH;
          case DEADLINE_EXPIRED_PATH -> DEADLINE_EXPIRED_PATH;
          case RANDOM -> randomPath();
        };
    paymentStates.put(paymentId, new State(paymentState, path, 0));

    Response response = paymentState.toResponse(createPaymentFormLink());

    return new DefaultApiResponses.InitApiResponse(response);
  }

  public List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> randomPath() {
    List<List<Function<PaymentState<?, ?>, PaymentState<?, ?>>>> paths =
        new ArrayList<>() {
          {
            add(HAPPY_PATH);
            add(HAPPY_PATH); // 50% chance of happy path
            add(REJECTED_PATH);
            add(DEADLINE_EXPIRED_PATH);
          }
        };

    return paths.get((int) (Math.random() * paths.size()));
  }

  private URI createPaymentFormLink() {
    return URI.create(
        String.format(
            "http://%s:%d",
            mockConfiguration.paymentForm().host(), mockConfiguration.paymentForm().port()));
  }
}
