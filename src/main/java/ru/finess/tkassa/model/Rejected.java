package ru.finess.tkassa.model;

import java.math.BigDecimal;
import ru.finess.openapi.model.GetState200Response;

public record Rejected(String terminalKey, BigDecimal amount, String orderId, String paymentId)
    implements PaymentState<Void, Void> {
  @Override
  public GetState200Response toGetStateResponse() {
    return new GetState200Response(
        terminalKey,
        amount,
        orderId,
        false,
        PaymentStatus.REJECTED.getStatus(),
        paymentId,
        "2",
        "Payment rejected",
        null,
        null);
  }

  @Override
  public Void toErrorState() {
    return null;
  }

  @Override
  public Void toOkState() {
    return null;
  }
}
