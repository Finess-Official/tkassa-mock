package ru.finess.tkassa.model;

import java.math.BigDecimal;
import ru.finess.openapi.model.GetState200Response;

public record Authorized(String terminalKey, BigDecimal amount, String orderId, String paymentId)
    implements PaymentState<Confirming, Void> {
  @Override
  public GetState200Response toGetStateResponse() {
    return new GetState200Response(
        terminalKey,
        amount,
        orderId,
        true,
        PaymentStatus.AUTHORIZED.getStatus(),
        paymentId,
        "0",
        null,
        null,
        null);
  }

  @Override
  public Void toErrorState() {
    return null;
  }

  @Override
  public Confirming toOkState() {
    return new Confirming(terminalKey, amount, orderId, paymentId);
  }
}
