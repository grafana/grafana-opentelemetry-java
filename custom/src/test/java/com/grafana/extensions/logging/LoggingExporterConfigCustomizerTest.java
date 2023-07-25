/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.logging;

import static com.grafana.extensions.logging.GrafanaLoggingConfig.DEBUG_LOGGING_PROP;
import static com.grafana.extensions.logging.GrafanaLoggingConfig.LOGGING_ENABLED_PROP;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoggingExporterConfigCustomizerTest {

  private static final String LOG_EXPORTER_PROP = "otel.logs.exporter";
  private static final String METRICS_EXPORTER_PROP = "otel.metrics.exporter";
  private static final String TRACES_EXPORTER_PROP = "otel.traces.exporter";

  record CustomTestCase(
      boolean debugLogging,
      String loggingEnabled,
      String exporterValue,
      Map<String, String> expectedReturn) {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCustomConfigurations")
  void getCustomProperties(String name, CustomTestCase testCase) {

    Map<String, String> props =
        Map.of(
            DEBUG_LOGGING_PROP,
            String.valueOf(testCase.debugLogging),
            LOGGING_ENABLED_PROP,
            testCase.loggingEnabled,
            "otel.logs.exporter",
            testCase.exporterValue,
            "otel.metrics.exporter",
            testCase.exporterValue,
            "otel.traces.exporter",
            testCase.exporterValue);
    DefaultConfigProperties defaultConfigs = DefaultConfigProperties.createForTest(props);
    Map<String, String> m = LoggingExporterConfigCustomizer.customizeProperties(defaultConfigs);

    assertThat(m).isEqualTo(testCase.expectedReturn);
  }

  private static Stream<Arguments> provideCustomConfigurations() {
    return Stream.of(
        Arguments.of(
            "debugLogging is on so logging for all signals",
            new CustomTestCase(
                true,
                "",
                "otlp",
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "debugLogging is on but logging exporter is already set",
            new CustomTestCase(
                true,
                "",
                "otlp,logging",
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "debugLogging takes precedence over loggingExporterEnabled",
            new CustomTestCase(
                true,
                "metrics",
                "otlp",
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "loggingExporterEnabled set with all signals",
            new CustomTestCase(
                false,
                "metrics,traces,logs",
                "otlp",
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "loggingExporterEnabled set with metrics,traces",
            new CustomTestCase(
                false,
                "metrics,traces",
                "otlp",
                Map.of(
                    LOG_EXPORTER_PROP, "otlp",
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "Logging cannot be appended since exporters are set to `none`",
            new CustomTestCase(
                false,
                "metrics,traces",
                "none",
                Map.of(
                    LOG_EXPORTER_PROP, "none",
                    METRICS_EXPORTER_PROP, "none",
                    TRACES_EXPORTER_PROP, "none"))),
        Arguments.of(
            "No logging enabled so no changes to exporter configurations",
            new CustomTestCase(false, "", "otlp", Map.of())));
  }

  record LoggingTestCase(
      boolean debugLogging, String loggingEnabled, Map<String, String> expectedReturn) {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideLoggingConfigurations")
  void getLoggingProperties(String name, LoggingTestCase testCase) {

    Map<String, String> props =
        Map.of(
            DEBUG_LOGGING_PROP,
            String.valueOf(testCase.debugLogging),
            LOGGING_ENABLED_PROP,
            testCase.loggingEnabled);
    DefaultConfigProperties defaultConfigs = DefaultConfigProperties.createForTest(props);
    GrafanaLoggingConfig logConfigs = new GrafanaLoggingConfig(defaultConfigs);

    Map<String, String> m = LoggingExporterConfigCustomizer.getLoggingExporterConfigs(logConfigs);
    assertThat(m).isEqualTo(testCase.expectedReturn);
  }

  private static Stream<Arguments> provideLoggingConfigurations() {
    Map<String, String> allSignals =
        Map.of(
            METRICS_EXPORTER_PROP,
            ",logging",
            TRACES_EXPORTER_PROP,
            ",logging",
            LOG_EXPORTER_PROP,
            ",logging");
    return Stream.of(
        Arguments.of("only debugLogging set to false", new LoggingTestCase(false, "", Map.of())),
        Arguments.of("only debugLogging set to true ", new LoggingTestCase(true, "", allSignals)),
        Arguments.of(
            "debugLogging set to true with metric logging enabled",
            new LoggingTestCase(true, "metrics", allSignals)),
        Arguments.of(
            "debugLogging set to true with `metrics,traces` logging enabled",
            new LoggingTestCase(true, "metrics,traces", allSignals)),
        Arguments.of(
            "debugLogging set to true with `metrics,traces,logs` logging enabled",
            new LoggingTestCase(true, "metrics,traces,logs", allSignals)),
        Arguments.of(
            "debugLogging set to false with `metric` logging enabled",
            new LoggingTestCase(
                false,
                "metrics",
                Map.of(
                    "otel.metrics.exporter", ",logging",
                    "otel.logs.exporter", "",
                    "otel.traces.exporter", ""))),
        Arguments.of(
            "debugLogging set to false with `metric,traces` logging enabled",
            new LoggingTestCase(
                false,
                "metrics,traces",
                Map.of(
                    "otel.metrics.exporter", ",logging",
                    "otel.logs.exporter", "",
                    "otel.traces.exporter", ",logging"))),
        Arguments.of(
            "debugLogging set to false with `metric,traces,logs` logging enabled",
            new LoggingTestCase(false, "metrics,traces,logs", allSignals)));
  }
}
