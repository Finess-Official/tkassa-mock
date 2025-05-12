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
}
