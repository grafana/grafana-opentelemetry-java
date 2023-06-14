/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaLoggingConfig.DEBUG_LOGGING_ENV;
import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaLoggingConfig.LOGGING_ENABLED_ENV;
import static com.grafana.extensions.resources.config.GrafanaConfig.normalizeEnvironmentVariableKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoggingExporterConfigCustomizerTest {

  @BeforeEach
  void clearSystemProperties() {
    System.clearProperty(DEBUG_LOGGING_PROP);
    System.clearProperty(LOGGING_ENABLED_PROP);
  }

  private static final String DEBUG_LOGGING_PROP =
      normalizeEnvironmentVariableKey(DEBUG_LOGGING_ENV);
  private static final String LOGGING_ENABLED_PROP =
      normalizeEnvironmentVariableKey(LOGGING_ENABLED_ENV);

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCustomConfigurations")
  void getCustomProperties(
      String name,
      String debugLogging,
      String loggingEnabled,
      String exporterValue,
      int expectedSize,
      ImmutableMap<String, String> expectedReturn) {

    System.setProperty(DEBUG_LOGGING_PROP, debugLogging);
    System.setProperty(LOGGING_ENABLED_PROP, loggingEnabled);

    ConfigProperties configs = mock(ConfigProperties.class);
    when(configs.getString("otel.logs.exporter", "otlp")).thenReturn(exporterValue);
    when(configs.getString("otel.metrics.exporter", "otlp")).thenReturn(exporterValue);
    when(configs.getString("otel.traces.exporter", "otlp")).thenReturn(exporterValue);
    Map<String, String> m = LoggingExporterConfigCustomizer.getCustomProperties(configs);

    assertThat(m.size()).isEqualTo(expectedSize);
    assertThat(m.equals(expectedReturn)).isTrue();
  }

  private static Stream<Arguments> provideCustomConfigurations() {
    ImmutableMap<String, String> overrides =
        ImmutableMap.of(
            "otel.metrics.exporter", "otlp,logging",
            "otel.traces.exporter", "otlp,logging",
            "otel.logs.exporter", "otlp,logging");

    return Stream.of(
        Arguments.of("debugLogging is on", "true", "", "otlp", 3, overrides),
        Arguments.of(
            "debugLogging is on",
            "true",
            "",
            "jaeger,otlp",
            3,
            ImmutableMap.of(
                "otel.metrics.exporter", "jaeger,otlp,logging",
                "otel.traces.exporter", "jaeger,otlp,logging",
                "otel.logs.exporter", "jaeger,otlp,logging")),
        Arguments.of(
            "Logging enabled for all signals",
            "false",
            "metrics,traces,logs",
            "otlp",
            3,
            overrides),
        Arguments.of(
            "Logging enabled for metrics,traces",
            "false",
            "metrics,traces",
            "otlp",
            3,
            ImmutableMap.of(
                "otel.logs.exporter", "otlp",
                "otel.metrics.exporter", "otlp,logging",
                "otel.traces.exporter", "otlp,logging")),
        Arguments.of(
            "Logging cannot be appended since exporters are set to `none`",
            "false",
            "metrics,traces",
            "none",
            3,
            ImmutableMap.of(
                "otel.logs.exporter", "none",
                "otel.metrics.exporter", "none",
                "otel.traces.exporter", "none")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideLoggingConfigurations")
  void getLoggingProperties(
      String name,
      String debugLogging,
      String loggingEnabled,
      int expectedSize,
      ImmutableMap<String, String> expectedReturn) {

    System.setProperty(DEBUG_LOGGING_PROP, debugLogging);
    System.setProperty(LOGGING_ENABLED_PROP, loggingEnabled);

    Map<String, String> m = LoggingExporterConfigCustomizer.getLoggingExporterConfigs();
    assertThat(m.size()).isEqualTo(expectedSize);
    assertThat(m.equals(expectedReturn)).isTrue();
  }

  private static Stream<Arguments> provideLoggingConfigurations() {
    ImmutableMap<String, String> allSignals =
        ImmutableMap.of(
            "otel.metrics.exporter", ",logging",
            "otel.traces.exporter", ",logging",
            "otel.logs.exporter", ",logging");
    return Stream.of(
        Arguments.of("only debugging logging set to false", "false", "", 0, ImmutableMap.of()),
        Arguments.of("only debugging logging set to true ", "true", "", 3, allSignals),
        Arguments.of(
            "debugging logging set to true with metric logging enabled",
            "true",
            "metrics",
            3,
            allSignals),
        Arguments.of(
            "debugging logging set to true with `metrics,traces` logging enabled",
            "true",
            "metrics,traces",
            3,
            allSignals),
        Arguments.of(
            "debugging logging set to true with `metrics,traces,logs` logging enabled",
            "true",
            "metrics,traces,logs",
            3,
            allSignals),
        Arguments.of(
            "debugging logging set to false with `metric` logging enabled",
            "false",
            "metrics",
            3,
            ImmutableMap.of(
                "otel.metrics.exporter", ",logging",
                "otel.logs.exporter", "",
                "otel.traces.exporter", "")),
        Arguments.of(
            "debugging logging set to false with `metric,traces` logging enabled",
            "false",
            "metrics,traces",
            3,
            ImmutableMap.of(
                "otel.metrics.exporter", ",logging",
                "otel.logs.exporter", "",
                "otel.traces.exporter", ",logging")),
        Arguments.of(
            "debugging logging set to false with `metric,traces,logs` logging enabled",
            "false",
            "metrics,traces,logs",
            3,
            allSignals));
  }
}
