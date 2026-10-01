/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.javaagent.extension.AgentListener;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link AutoConfigureUtil#getConfig} returns null under declarative configuration, so every {@link
 * AgentListener} must handle that without failing agent startup.
 *
 * <p>https://github.com/grafana/grafana-opentelemetry-java/issues/1382
 */
class AgentListenerDeclarativeConfigTest {

  @AfterEach
  void tearDown() {
    System.clearProperty("otel.config.file");
  }

  @Test
  void allAgentListenersSupportDeclarativeConfig(@TempDir Path tempDir) throws IOException {
    List<AgentListener> listeners = new ArrayList<>();
    ServiceLoader.load(AgentListener.class).forEach(listeners::add);
    assertThat(listeners).isNotEmpty();

    Path configFile = tempDir.resolve("otel.yaml");
    Files.write(configFile, "file_format: \"1.0\"\n".getBytes());
    // declarative config is only detected from system properties / env vars
    System.setProperty("otel.config.file", configFile.toString());

    AutoConfiguredOpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.builder().build();
    try {
      assertThat(AutoConfigureUtil.getConfig(sdk)).isNull();
      for (AgentListener listener : listeners) {
        assertThatCode(() -> listener.afterAgent(sdk))
            .as(listener.getClass().getName())
            .doesNotThrowAnyException();
      }
    } finally {
      sdk.getOpenTelemetrySdk().close();
    }
  }
}
