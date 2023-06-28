/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaLoggingConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import com.grafana.extensions.resources.config.GrafanaConfig.GrafanaLoggingConfig;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoggingExporterConfigCustomizerTest {

  private static final String LOG_EXPORTER_PROP = "otel.logs.exporter";
  private static final String METRICS_EXPORTER_PROP = "otel.metrics.exporter";
  private static final String TRACES_EXPORTER_PROP = "otel.traces.exporter";

  @BeforeEach
  void clearSystemProperties() {
    System.clearProperty(DEBUG_LOGGING_PROP);
    System.clearProperty(LOGGING_ENABLED_PROP);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCustomConfigurations")
  void getCustomProperties(
      String name,
      boolean debugLogging,
      String loggingEnabled,
      String exporterValue,
      int expectedSize,
      ImmutableMap<String, String> expectedReturn) {

    ConfigProperties configs = mock(ConfigProperties.class);
    when(configs.getBoolean(DEBUG_LOGGING_PROP, Boolean.FALSE)).thenReturn(debugLogging);
    when(configs.getString(LOGGING_ENABLED_PROP, "")).thenReturn(loggingEnabled);
    when(configs.getString("otel.logs.exporter", "otlp")).thenReturn(exporterValue);
    when(configs.getString("otel.metrics.exporter", "otlp")).thenReturn(exporterValue);
    when(configs.getString("otel.traces.exporter", "otlp")).thenReturn(exporterValue);

    Map<String, String> m = LoggingExporterConfigCustomizer.getCustomProperties(configs);

    assertThat(m.size()).isEqualTo(expectedSize);
    assertThat(m.equals(expectedReturn)).isTrue();
  }

  private static Stream<Arguments> provideCustomConfigurations() {
    return Stream.of(
        Arguments.of(
            "debugLogging is on",
            Boolean.TRUE,
            "",
            "otlp",
            3,
            ImmutableMap.of(
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging",
                LOG_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "debugLogging is on",
            Boolean.TRUE,
            "",
            "jaeger,otlp",
            3,
            ImmutableMap.of(
                METRICS_EXPORTER_PROP, "jaeger,otlp,logging",
                TRACES_EXPORTER_PROP, "jaeger,otlp,logging",
                LOG_EXPORTER_PROP, "jaeger,otlp,logging")),
        Arguments.of(
            "Logging enabled for all signals",
            Boolean.FALSE,
            "metrics,traces,logs",
            "otlp",
            3,
            ImmutableMap.of(
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging",
                LOG_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "Logging enabled for metrics,traces",
            Boolean.FALSE,
            "metrics,traces",
            "otlp",
            3,
            ImmutableMap.of(
                LOG_EXPORTER_PROP, "otlp",
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "Logging cannot be appended since exporters are set to `none`",
            Boolean.FALSE,
            "metrics,traces",
            "none",
            3,
            ImmutableMap.of(
                LOG_EXPORTER_PROP, "none",
                METRICS_EXPORTER_PROP, "none",
                TRACES_EXPORTER_PROP, "none")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideLoggingConfigurations")
  void getLoggingProperties(
      String name,
      boolean debugLogging,
      String loggingEnabled,
      int expectedSize,
      ImmutableMap<String, String> expectedReturn) {

    ConfigProperties configs = mock(ConfigProperties.class);
    when(configs.getBoolean(DEBUG_LOGGING_PROP, Boolean.FALSE)).thenReturn(debugLogging);
    when(configs.getString(LOGGING_ENABLED_PROP, "")).thenReturn(loggingEnabled);
    GrafanaLoggingConfig logConfigs = new GrafanaLoggingConfig(configs);

    Map<String, String> m = LoggingExporterConfigCustomizer.getLoggingExporterConfigs(logConfigs);
    assertThat(m.size()).isEqualTo(expectedSize);
    assertThat(m.equals(expectedReturn)).isTrue();
  }

  private static Stream<Arguments> provideLoggingConfigurations() {
    ImmutableMap<String, String> allSignals =
        ImmutableMap.of(
            METRICS_EXPORTER_PROP,
            ",logging",
            TRACES_EXPORTER_PROP,
            ",logging",
            LOG_EXPORTER_PROP,
            ",logging");
    return Stream.of(
        Arguments.of("only debugging logging set to false", false, "", 0, ImmutableMap.of()),
        Arguments.of("only debugging logging set to true ", true, "", 3, allSignals),
        Arguments.of(
            "debugging logging set to true with metric logging enabled",
            Boolean.TRUE,
            "metrics",
            3,
            allSignals),
        Arguments.of(
            "debugging logging set to true with `metrics,traces` logging enabled",
            Boolean.TRUE,
            "metrics,traces",
            3,
            allSignals),
        Arguments.of(
            "debugging logging set to true with `metrics,traces,logs` logging enabled",
            true,
            "metrics,traces,logs",
            3,
            allSignals),
        Arguments.of(
            "debugging logging set to false with `metric` logging enabled",
            Boolean.FALSE,
            "metrics",
            3,
            ImmutableMap.of(
                "otel.metrics.exporter", ",logging",
                "otel.logs.exporter", "",
                "otel.traces.exporter", "")),
        Arguments.of(
            "debugging logging set to false with `metric,traces` logging enabled",
            Boolean.FALSE,
            "metrics,traces",
            3,
            ImmutableMap.of(
                "otel.metrics.exporter", ",logging",
                "otel.logs.exporter", "",
                "otel.traces.exporter", ",logging")),
        Arguments.of(
            "debugging logging set to false with `metric,traces,logs` logging enabled",
            Boolean.FALSE,
            "metrics,traces,logs",
            3,
            allSignals));
  }
}
