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

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
        .addPropertiesSupplier(GrafanaAutoConfigCustomizerProvider::getDefaultProperties)
        .addPropertiesCustomizer(TestedInstrumentationsCustomizer::customizeProperties)
        .addMeterProviderCustomizer(MetricsCustomizer::configure)
        .addResourceCustomizer(ResourceCustomizer::truncate);
  }

  private static Map<String, String> getDefaultProperties() {
    HashMap<String, String> map = new HashMap<>();
    map.put("otel.instrumentation.micrometer.base-time-unit", "s");
    map.put("otel.instrumentation.log4j-appender.experimental-log-attributes", "true");
    map.put("otel.instrumentation.logback-appender.experimental-log-attributes", "true");
    return map;
  }
}
