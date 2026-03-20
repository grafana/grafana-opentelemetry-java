/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import com.grafana.extensions.filter.MetricsCustomizer;
import com.grafana.extensions.instrumentations.TestedInstrumentationsCustomizer;
import com.grafana.extensions.resources.ResourceCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import java.util.HashMap;
import java.util.Map;

public class GrafanaAutoConfigCustomizerProvider implements AutoConfigurationCustomizerProvider {

  // Defaults in DC notation (source of truth).
  // Keys are DC paths under instrumentation/development.java.
  // ConfigPropertiesBackedConfigProvider bridges these to ConfigProperties automatically.
  static final Map<String, String> DC_DEFAULTS = new HashMap<>();

  static {
    DC_DEFAULTS.put("micrometer.base_time_unit", "s");
    DC_DEFAULTS.put("log4j_appender.experimental_log_attributes", "true");
    DC_DEFAULTS.put("logback_appender.experimental_log_attributes", "true");
  }

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
        .addPropertiesSupplier(GrafanaAutoConfigCustomizerProvider::getDefaultConfigProperties)
        .addPropertiesCustomizer(TestedInstrumentationsCustomizer::customizeProperties)
        .addMeterProviderCustomizer(MetricsCustomizer::configure)
        .addResourceCustomizer(ResourceCustomizer::truncate);
  }

  /** Translate DC defaults to {@code otel.instrumentation.*} keys for auto-configuration. */
  private static Map<String, String> getDefaultConfigProperties() {
    HashMap<String, String> map = new HashMap<>();
    for (Map.Entry<String, String> entry : DC_DEFAULTS.entrySet()) {
      map.put("otel.instrumentation." + entry.getKey().replace('_', '-'), entry.getValue());
    }
    return map;
  }
}
