/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import com.google.common.collect.ImmutableSet;
import com.grafana.extensions.resources.config.GrafanaConfig.GrafanaLoggingConfig;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class LoggingExporterConfigCustomizer {

  private static final Logger logger =
      Logger.getLogger(LoggingExporterConfigCustomizer.class.getName());
  private static final ImmutableSet<String> SIGNAL_TYPES =
      ImmutableSet.of("traces", "metrics", "logs");

  static Map<String, String> getCustomProperties(ConfigProperties configs) {
    GrafanaLoggingConfig logConfigs = new GrafanaLoggingConfig(configs);
    Map<String, String> logging = getLoggingExporterConfigs(logConfigs);
    Map<String, String> overrides = new HashMap<>();
    if (!logging.isEmpty()) {
      for (String signal : SIGNAL_TYPES) {
        String propName = getOtelExporterPropName(signal);

        // Logging cannot be added to the signal's exporters if the exporter for the signal
        // is set to `none`.
        // Note that 'otel.logs.exporter' seem to differ from the other signals in that the
        // initial value is set to 'none'.
        String exporters = configs.getString(propName, "otlp");
        exporters =
            !(exporters.equals("none")) && !exporters.contains("logging")
                ? exporters.concat(logging.get(propName))
                : exporters;
        overrides.put(propName, exporters);
      }
    }
    logger.info("Property overrides:  " + overrides);
    return overrides;
  }

  public static Map<String, String> getLoggingExporterConfigs(GrafanaLoggingConfig configs) {
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
    logger.info("Logging status: " + loggingConfig);
    return loggingConfig;
  }

  private static String getOtelExporterPropName(String signalType) {
    return String.format("otel.%s.exporter", signalType.toLowerCase());
  }
}
