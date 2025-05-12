package ru.finess.tkassa.model;

import java.math.BigDecimal;
import ru.finess.openapi.model.GetState200Response;

public record Confirming(String terminalKey, BigDecimal amount, String orderId, String paymentId)
    implements PaymentState<Confirmed, Void> {
  @Override
  public GetState200Response toGetStateResponse() {
    return new GetState200Response(
        terminalKey,
        amount,
        orderId,
        true,
        PaymentStatus.CONFIRMING.getStatus(),
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
  public Confirmed toOkState() {
    return new Confirmed(terminalKey, amount, orderId, paymentId);
  }
}
