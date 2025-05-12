package ru.finess.tkassa.model;

import java.math.BigDecimal;
import ru.finess.openapi.model.GetState200Response;

public record Authorizing(String terminalKey, BigDecimal amount, String orderId, String paymentId)
    implements PaymentState<Authorized, Rejected> {
  @Override
  public GetState200Response toGetStateResponse() {
    return new GetState200Response(
        terminalKey,
        amount,
        orderId,
        true,
        PaymentStatus.AUTHORIZING.getStatus(),
        paymentId,
        "0",
        null,
        null,
        null);
  }

  @Override
  public Rejected toErrorState() {
    return new Rejected(terminalKey, amount, orderId, paymentId);
  }

  @Override
  public Authorized toOkState() {
    return new Authorized(terminalKey, amount, orderId, paymentId);
  }
}
