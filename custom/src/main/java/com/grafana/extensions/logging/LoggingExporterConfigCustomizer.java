/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.logging;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class LoggingExporterConfigCustomizer {

  private static final Logger logger =
      Logger.getLogger(LoggingExporterConfigCustomizer.class.getName());
  private static final Set<String> SIGNAL_TYPES =
      new HashSet<>(Arrays.asList("traces", "metrics", "logs"));

  public static Map<String, String> customizeProperties(ConfigProperties configs) {
    return customizeProperties(configs, DefaultConfigProperties.create(Collections.emptyMap()));
  }

  static Map<String, String> customizeProperties(
      ConfigProperties configs, ConfigProperties userConfigs) {
    GrafanaLoggingConfig logConfigs = new GrafanaLoggingConfig(configs);
    Map<String, String> advice = getAdviceExporters(logConfigs);
    Map<String, String> overrides = new HashMap<>();
    for (String signal : SIGNAL_TYPES) {
      String propName = getOtelExporterPropName(signal);

      // Logging cannot be added to the signal's exporters if the exporter for the signal
      // is set to `none`.
      // We need to check the user supplied config directly, as a workaround, because
      // the logging exporter is initialized to "none" in
      // https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/acbab58a4696169802595e75e738572685abad0c/javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/OpenTelemetryInstaller.java#L33
      String userExporter = userConfigs.getString(propName, "otlp");
      String adviceExporter = advice.get(propName);
      String exporter =
          userExporter.equals("none") || userExporter.contains("logging") || adviceExporter == null
              ? userExporter
              : userExporter.concat(adviceExporter);
      overrides.put(propName, exporter);
    }
    logger.info("Property overrides:  " + overrides);
    return overrides;
  }

  public static Map<String, String> getAdviceExporters(GrafanaLoggingConfig configs) {
    List<String> signalsToEnable;
    if (configs.isDebugLogging()) {
      signalsToEnable = new ArrayList<>(SIGNAL_TYPES);
    } else {
      signalsToEnable = configs.getLoggingEnabled();
    }
    if (signalsToEnable.isEmpty()) {
      return new HashMap<>();
    }

    // init logging config map
    Map<String, String> loggingConfig =
        SIGNAL_TYPES.stream()
            .collect(
                Collectors.toMap(
                    LoggingExporterConfigCustomizer::getOtelExporterPropName, v -> ""));
    for (String signal : signalsToEnable) {
      if (SIGNAL_TYPES.contains(signal.toLowerCase())) {
        loggingConfig.put(getOtelExporterPropName(signal), ",logging");
      }
    }
    logger.fine("Logging status: " + loggingConfig);
    return loggingConfig;
  }

  private static String getOtelExporterPropName(String signalType) {
    return String.format("otel.%s.exporter", signalType.toLowerCase());
  }
}
