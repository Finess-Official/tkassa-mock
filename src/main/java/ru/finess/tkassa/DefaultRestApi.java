package ru.finess.tkassa;

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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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

  private final MockConfiguration mockConfiguration;
  private final InmemoryPaymentRepository paymentRepository;

  public DefaultRestApi(
      MockConfiguration mockConfiguration, InmemoryPaymentRepository paymentRepository) {
    this.mockConfiguration = mockConfiguration;
    this.paymentRepository = paymentRepository;
  }

  @Override
  public DefaultApiResponses.GetStateApiResponse getState(GetStateFULL getStateFULL)
      throws Exception {
    String paymentId = getStateFULL.paymentId();

    PaymentState<?, ?> currentState =
        paymentRepository
            .findState(paymentId)
            .orElseThrow(() -> HttpServerResponseException.of(404, "Payment not found"));

    if (!currentState.isFinal()) {
      currentState = paymentRepository.nextState(paymentId).orElse(currentState);
    }

    GetState200Response response = currentState.toGetStateResponse();
    return new DefaultApiResponses.GetStateApiResponse(response);
  }

  @Override
  public DefaultApiResponses.InitApiResponse init(InitFULL initFULL) throws Exception {
    String paymentId = UUID.randomUUID().toString().substring(0, 20);
    New paymentState =
        new New(initFULL.terminalKey(), initFULL.amount(), initFULL.orderId(), paymentId);

    List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> path =
        switch (mockConfiguration.paymentTransitionStrategy()) {
          case HAPPY_PATH -> HAPPY_PATH;
          case REJECTED_PATH -> REJECTED_PATH;
          case DEADLINE_EXPIRED_PATH -> DEADLINE_EXPIRED_PATH;
          case RANDOM -> randomPath();
        };
    paymentRepository.save(paymentState, path);

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
