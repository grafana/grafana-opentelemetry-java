/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerTimingHeaderActivatorTest {

  private final ServerTimingHeaderActivator activator = new ServerTimingHeaderActivator();

  @BeforeEach
  @AfterEach
  void reset() {
    ServerTimingHeaderCustomizer.enabled = false;
    System.clearProperty("otel.config.file");
  }

  @Test
  void enabledByDefault() {
    activate(Map.of());

    assertThat(ServerTimingHeaderCustomizer.enabled).isTrue();
  }

  @Test
  void disabledByProperty() {
    activate(Map.of("grafana.otel.trace-response-header.enabled", "false"));

    assertThat(ServerTimingHeaderCustomizer.enabled).isFalse();
  }

  // https://github.com/grafana/grafana-opentelemetry-java/issues/1382
  @Test
  void disabledWithDeclarativeConfig(@TempDir Path tempDir) throws IOException {
    Path configFile = tempDir.resolve("otel.yaml");
    Files.write(configFile, "file_format: \"1.0\"\n".getBytes());

    // declarative config is only detected from system properties / env vars
    System.setProperty("otel.config.file", configFile.toString());

    activate(Map.of());

    assertThat(ServerTimingHeaderCustomizer.enabled).isFalse();
  }

  private void activate(Map<String, String> properties) {
    Map<String, String> config = new HashMap<>(properties);
    config.put("otel.traces.exporter", "none");
    config.put("otel.metrics.exporter", "none");
    config.put("otel.logs.exporter", "none");
    AutoConfiguredOpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder().addPropertiesSupplier(() -> config).build();
    try {
      activator.afterAgent(sdk);
    } finally {
      sdk.getOpenTelemetrySdk().close();
    }
  }
}
