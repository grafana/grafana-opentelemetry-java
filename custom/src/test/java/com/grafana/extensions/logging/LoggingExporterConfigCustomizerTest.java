/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.logging;

import static com.grafana.extensions.logging.GrafanaLoggingConfig.DEBUG_LOGGING_PROP;
import static com.grafana.extensions.logging.GrafanaLoggingConfig.LOGGING_ENABLED_PROP;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
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

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCustomConfigurations")
  void getCustomProperties(
      String name,
      boolean debugLogging,
      String loggingEnabled,
      String exporterValue,
      ImmutableMap<String, String> expectedReturn) {

    Map<String, String> props =
        ImmutableMap.of(
            DEBUG_LOGGING_PROP,
            String.valueOf(debugLogging),
            LOGGING_ENABLED_PROP,
            loggingEnabled,
            "otel.logs.exporter",
            exporterValue,
            "otel.metrics.exporter",
            exporterValue,
            "otel.traces.exporter",
            exporterValue);
    DefaultConfigProperties defaultConfigs = DefaultConfigProperties.createForTest(props);
    Map<String, String> m = LoggingExporterConfigCustomizer.customizeProperties(defaultConfigs);

    assertThat(m.size()).isEqualTo(expectedReturn.size());
    assertThat(m).isEqualTo(expectedReturn);
  }

  private static Stream<Arguments> provideCustomConfigurations() {
    return Stream.of(
        Arguments.of(
            "debugLogging is on so logging for all signals",
            true,
            "",
            "otlp",
            ImmutableMap.of(
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging",
                LOG_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "debugLogging is on but logging exporter is already set",
            true,
            "",
            "otlp,logging",
            ImmutableMap.of(
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging",
                LOG_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "debugLogging takes precedence over loggingExporterEnabled",
            true,
            "metrics",
            "otlp",
            ImmutableMap.of(
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging",
                LOG_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "loggingExporterEnabled set with all signals",
            false,
            "metrics,traces,logs",
            "otlp",
            ImmutableMap.of(
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging",
                LOG_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "loggingExporterEnabled set with metrics,traces",
            false,
            "metrics,traces",
            "otlp",
            ImmutableMap.of(
                LOG_EXPORTER_PROP, "otlp",
                METRICS_EXPORTER_PROP, "otlp,logging",
                TRACES_EXPORTER_PROP, "otlp,logging")),
        Arguments.of(
            "Logging cannot be appended since exporters are set to `none`",
            false,
            "metrics,traces",
            "none",
            ImmutableMap.of(
                LOG_EXPORTER_PROP, "none",
                METRICS_EXPORTER_PROP, "none",
                TRACES_EXPORTER_PROP, "none")),
        Arguments.of(
            "No logging enabled so no changes to exporter configurations",
            false,
            "",
            "otlp",
            ImmutableMap.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideLoggingConfigurations")
  void getLoggingProperties(
      String name,
      boolean debugLogging,
      String loggingEnabled,
      ImmutableMap<String, String> expectedReturn) {

    Map<String, String> props =
        ImmutableMap.of(
            DEBUG_LOGGING_PROP, String.valueOf(debugLogging), LOGGING_ENABLED_PROP, loggingEnabled);
    DefaultConfigProperties defaultConfigs = DefaultConfigProperties.createForTest(props);
    GrafanaLoggingConfig logConfigs = new GrafanaLoggingConfig(defaultConfigs);

    Map<String, String> m = LoggingExporterConfigCustomizer.getLoggingExporterConfigs(logConfigs);
    assertThat(m.size()).isEqualTo(expectedReturn.size());
    assertThat(m).isEqualTo(expectedReturn);
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
        Arguments.of("only debugLogging set to false", false, "", ImmutableMap.of()),
        Arguments.of("only debugLogging set to true ", true, "", allSignals),
        Arguments.of(
            "debugLogging set to true with metric logging enabled", true, "metrics", allSignals),
        Arguments.of(
            "debugLogging set to true with `metrics,traces` logging enabled",
            true,
            "metrics,traces",
            allSignals),
        Arguments.of(
            "debugLogging set to true with `metrics,traces,logs` logging enabled",
            true,
            "metrics,traces,logs",
            allSignals),
        Arguments.of(
            "debugLogging set to false with `metric` logging enabled",
            false,
            "metrics",
            ImmutableMap.of(
                "otel.metrics.exporter", ",logging",
                "otel.logs.exporter", "",
                "otel.traces.exporter", "")),
        Arguments.of(
            "debugLogging set to false with `metric,traces` logging enabled",
            false,
            "metrics,traces",
            ImmutableMap.of(
                "otel.metrics.exporter", ",logging",
                "otel.logs.exporter", "",
                "otel.traces.exporter", ",logging")),
        Arguments.of(
            "debugLogging set to false with `metric,traces,logs` logging enabled",
            false,
            "metrics,traces,logs",
            allSignals));
  }
}
