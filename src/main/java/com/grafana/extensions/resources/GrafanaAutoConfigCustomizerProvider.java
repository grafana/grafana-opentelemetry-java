/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * This is one of the main entry points for Instrumentation Agent's customizations. It allows
 * configuring the {@link AutoConfigurationCustomizer}. See the {@link
 * #customize(AutoConfigurationCustomizer)} method below.
 *
 * @see AutoConfigurationCustomizerProvider
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class GrafanaAutoConfigCustomizerProvider implements AutoConfigurationCustomizerProvider {

  private static final Logger logger =
      Logger.getLogger(GrafanaAutoConfigCustomizerProvider.class.getName());
  private static final ImmutableSet<String> SIGNAL_TYPES =
      ImmutableSet.of("traces", "metrics", "logs");
  private static final String LOGGING_ENV_VAR_NAME = "GRAFANA_OTEL_LOGGING_EXPORTER_ENABLED";
  private static final String LOGGING_SYS_PROP_NAME =
      LOGGING_ENV_VAR_NAME.toLowerCase().replace("_", ".");

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    Map<String, String> logging = getLoggingExporterConfigs();
    if (logging != null && !logging.isEmpty()) {
      autoConfiguration.addPropertiesCustomizer(
          config -> {
            Map<String, String> overrides = new HashMap<>();
            for (String signal : SIGNAL_TYPES) {
              String propName = getOtelExporterPropName(signal);

              // otel.logs.exporter seem to differ from the other signals in that the initial value
              // is set to 'none'
              // The following will resolve the issue by overriding the 'none' with 'otlp' however,
              // there is an issue
              // with integration fake backend which has trouble with the logs otlp exporter.
              //
              // if (propName.equals(LOGS_EXPORTER)) {
              //  overrides.put(
              //      propName,
              //      config
              //          .getString(propName, "")
              //          .replace("none", "otlp")
              //          .concat(logging.get(propName)));
              // } else { ...
              //

              overrides.put(
                  propName, config.getString(propName, "otlp").concat(logging.get(propName)));

              // }

            }
            logger.info("Property override:  " + overrides);
            return overrides;
          });
    }
  }

  public static Map<String, String> getLoggingExporterConfigs() {
    Map<String, String> loggingConfig = new HashMap<>();
    // system prop takes precedence over env variable
    String propLoggingEnabled = System.getProperty(LOGGING_SYS_PROP_NAME);
    String signalsToEnable =
        StringUtils.isNotBlank(propLoggingEnabled)
            ? propLoggingEnabled
            : System.getenv(LOGGING_ENV_VAR_NAME);
    logger.info("Logging to be enabled for: " + signalsToEnable);
    if (StringUtils.isNotBlank(signalsToEnable)) {
      for (String type : SIGNAL_TYPES) { // init logging config
        loggingConfig.put(getOtelExporterPropName(type), "");
      }
      String[] signals = signalsToEnable.toLowerCase().split(",");
      for (String signal : signals) {
        if (SIGNAL_TYPES.contains(signal)) {
          loggingConfig.put(getOtelExporterPropName(signal), ",logging");
        }
      }
      logger.info("Logging status: " + loggingConfig);
    }
    return loggingConfig;
  }

  private static String getOtelExporterPropName(String signalType) {
    return String.format("otel.%s.exporter", signalType);
  }
}
