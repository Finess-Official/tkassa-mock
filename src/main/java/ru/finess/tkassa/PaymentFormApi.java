package ru.finess.tkassa;

import lombok.extern.slf4j.Slf4j;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.body.HttpBodyOutput;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

@Slf4j
@Component
@HttpController
public class PaymentFormApi {

  @HttpRoute(method = HttpMethod.GET, path = "/payment/form/{paymentId}")
  public HttpServerResponse getForm(@Path("paymentId") String paymentId) throws Exception {
    return HttpServerResponse.of(
        200,
        HttpBodyOutput.of(
            "text/html",
            this.getClass().getClassLoader().getResourceAsStream("templates/index.html")));
  }

  @HttpRoute(method = HttpMethod.GET, path = "/payment/{paymentId}/amount")
  public HttpServerResponse getAmount(@Path("paymentId") String paymentId) {
    return HttpServerResponse.of(
        200,
        HttpBody.json(
            """
                    {
                      "amount" : 12000
                    }
                """));
  }

  @HttpRoute(method = HttpMethod.GET, path = "/payment/{paymentId}/deeplink")
  public HttpServerResponse getDeeplink() {
    return HttpServerResponse.of(
        200,
        HttpBody.json(
            """
                    {
                      "deeplink" : "finesspay://tkassa?state=success"
                    }
                """));
  }
}
