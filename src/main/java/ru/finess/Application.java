package ru.finess;

import java.net.URI;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.yaml.YamlConfigModule;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.common.JsonReader;
import ru.tinkoff.kora.json.common.JsonWriter;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.validation.module.ValidationModule;

@KoraApp
public interface Application
    extends UndertowHttpServerModule, YamlConfigModule, ValidationModule, JsonModule {

  static void main(String[] args) {
    KoraApplication.run(ApplicationGraph::graph);
  }

  default JsonWriter<URI> uriJsonWriter() {
    return (generator, value) -> {
      if (value == null) {
        generator.writeNull();
      } else {
        generator.writeString(value.toString());
      }
    };
  }

  default JsonReader<URI> uriJsonReader() {
    return (parser) -> {
      if (parser.currentToken() == null) {
        return null;
      }
      if (parser.currentToken().isScalarValue()) {
        return URI.create(parser.currentToken().asString());
      } else {
        throw new IllegalStateException("Expected string token");
      }
    };
  }
}
