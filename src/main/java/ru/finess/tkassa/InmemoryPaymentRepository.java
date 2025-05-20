package ru.finess.tkassa;

import ru.finess.tkassa.model.New;
import ru.finess.tkassa.model.PaymentState;
import ru.tinkoff.kora.common.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class InmemoryPaymentRepository {

  private final ConcurrentHashMap<String, ApplicationState> paymentStates =
      new ConcurrentHashMap<>();

  public Optional<PaymentState<?, ?>> findState(String paymentId) {
    ApplicationState state = paymentStates.get(paymentId);
    if (state == null) {
      return Optional.empty();
    }

    return Optional.of(state.currentState());
  }

  public Optional<PaymentState<?, ?>> nextState(String paymentId) {
    ApplicationState state = paymentStates.get(paymentId);
    if (state == null) {
      return Optional.empty();
    }

    ApplicationState nextState = state.nextState();
    paymentStates.put(paymentId, nextState);
    return Optional.ofNullable(nextState.currentState());
  }

  public void save(New paymentState, List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> path) {
    ApplicationState state = new ApplicationState(paymentState, path, 0);
    paymentStates.put(paymentState.paymentId(), state);
  }
}
