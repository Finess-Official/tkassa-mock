package ru.finess.tkassa;

import java.util.List;
import java.util.function.Function;
import ru.finess.tkassa.model.PaymentState;

record ApplicationState(
    PaymentState<?, ?> currentState,
    List<Function<PaymentState<?, ?>, PaymentState<?, ?>>> stateTransitions,
    int stateTransitionIndex) {

  public ApplicationState nextState() {
    Function<PaymentState<?, ?>, PaymentState<?, ?>> transition =
        stateTransitions.get(stateTransitionIndex);
    return new ApplicationState(
        transition.apply(currentState), stateTransitions, stateTransitionIndex + 1);
  }
}
