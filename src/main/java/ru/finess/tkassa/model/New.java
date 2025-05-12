package ru.finess.tkassa.model;

import java.math.BigDecimal;
import java.net.URI;
import ru.finess.openapi.model.GetState200Response;
import ru.finess.openapi.model.Response;

public record New(String terminalKey, BigDecimal amount, String orderId, String paymentId)
    implements PaymentState<Authorizing, DeadlineExpired> {
  @Override
  public GetState200Response toGetStateResponse() {
    return new GetState200Response(
        terminalKey,
        amount,
        orderId,
        true,
        PaymentStatus.NEW.getStatus(),
        paymentId,
        "0",
        null,
        null,
        null);
  }

  public Response toResponse(URI currentHost) {
    return new Response(
        terminalKey,
        amount,
        orderId,
        true,
        PaymentStatus.NEW.getStatus(),
        paymentId,
        "0",
        currentHost.resolve("/payment/form/" + paymentId),
        null,
        null);
  }

  @Override
  public DeadlineExpired toErrorState() {
    return new DeadlineExpired(terminalKey, amount, orderId, paymentId);
  }

  @Override
  public Authorizing toOkState() {
    return new Authorizing(terminalKey, amount, orderId, paymentId);
  }
}
