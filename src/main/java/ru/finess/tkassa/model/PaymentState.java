package ru.finess.tkassa.model;

import ru.finess.openapi.model.GetState200Response;

public sealed interface PaymentState<OK, ERROR>
    permits New,
        Authorizing,
        Authorized,
        Confirming,
        Confirmed,
        DeadlineExpired,
        Rejected,
        AuthFail {

  GetState200Response toGetStateResponse();

  ERROR toErrorState();

  OK toOkState();

  default boolean isFinal() {
    return switch (this) {
      case AuthFail authFail -> true;
      case Confirmed confirmed -> true;
      case DeadlineExpired deadlineExpired -> true;
      case Rejected rejected -> true;
      case Authorized authorized -> false;
      case Authorizing authorizing -> false;
      case Confirming confirming -> false;
      case New aNew -> false;
    };
  }
}
