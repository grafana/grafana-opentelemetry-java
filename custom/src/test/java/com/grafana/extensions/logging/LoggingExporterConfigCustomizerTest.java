/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.logging;

import static com.grafana.extensions.logging.GrafanaLoggingConfig.DEBUG_LOGGING_PROP;
import static com.grafana.extensions.logging.GrafanaLoggingConfig.LOGGING_ENABLED_PROP;
import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.instrumentations.TestedInstrumentationsCustomizer;
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

  record TestCase(Map<String, String> userConfigs, Map<String, String> want) {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCustomConfigurations")
  void getCustomProperties(String name, TestCase testCase) {

    DefaultConfigProperties configProperties =
        DefaultConfigProperties.createFromMap(Map.of("otel.logs.exporter", "none"))
            .withOverrides(testCase.userConfigs); // see
    // https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/acbab58a4696169802595e75e738572685abad0c/javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/OpenTelemetryInstaller.java#L33

    configProperties =
        configProperties.withOverrides(
            LoggingExporterConfigCustomizer.customizeProperties(
                configProperties, DefaultConfigProperties.createFromMap(testCase.userConfigs)));

    assertThat(TestedInstrumentationsCustomizer.getAllProperties(configProperties))
        .containsAllEntriesOf(testCase.want);
  }

  private static Stream<Arguments> provideCustomConfigurations() {
    return Stream.of(
        Arguments.of(
            "otlp by default",
            new TestCase(
                Map.of(),
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp",
                    TRACES_EXPORTER_PROP, "otlp",
                    LOG_EXPORTER_PROP, "otlp"))),
        Arguments.of(
            "debugLogging is on",
            new TestCase(
                Map.of(DEBUG_LOGGING_PROP, "true"),
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "debugLogging is on but logging exporter is already set",
            new TestCase(
                Map.of(
                    DEBUG_LOGGING_PROP,
                    "true",
                    METRICS_EXPORTER_PROP,
                    "otlp,logging",
                    TRACES_EXPORTER_PROP,
                    "otlp,logging",
                    LOG_EXPORTER_PROP,
                    "otlp,logging"),
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "loggingExporterEnabled set with all signals",
            new TestCase(
                Map.of(LOGGING_ENABLED_PROP, "metrics,traces,logs"),
                Map.of(
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "loggingExporterEnabled set with metrics,traces",
            new TestCase(
                Map.of(LOGGING_ENABLED_PROP, "metrics,traces"),
                Map.of(
                    LOG_EXPORTER_PROP, "otlp",
                    METRICS_EXPORTER_PROP, "otlp,logging",
                    TRACES_EXPORTER_PROP, "otlp,logging"))),
        Arguments.of(
            "Logging cannot be appended for exporter that is manually set to `none`",
            new TestCase(
                Map.of(DEBUG_LOGGING_PROP, "true", METRICS_EXPORTER_PROP, "none"),
                Map.of(
                    METRICS_EXPORTER_PROP, "none",
                    TRACES_EXPORTER_PROP, "otlp,logging",
                    LOG_EXPORTER_PROP, "otlp,logging"))));
  }
}
