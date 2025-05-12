package ru.finess.tkassa;

import ru.tinkoff.kora.config.common.annotation.ConfigSource;
import ru.tinkoff.kora.config.common.annotation.ConfigValueExtractor;

@ConfigSource("finess.tkassa.mock")
public interface MockConfiguration {

  enum PaymentTransitionStrategy {
    HAPPY_PATH,
    REJECTED_PATH,
    DEADLINE_EXPIRED_PATH,
    RANDOM
  }

  PaymentTransitionStrategy paymentTransitionStrategy();

  @ConfigValueExtractor
  interface PaymentForm {
    String host();

    int port();
  }

  PaymentForm paymentForm();
}
