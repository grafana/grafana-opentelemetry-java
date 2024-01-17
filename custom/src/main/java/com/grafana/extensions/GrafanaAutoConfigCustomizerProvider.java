/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import com.grafana.extensions.cloud.GrafanaCloudConfigCustomizer;
import com.grafana.extensions.filter.MetricFilter;
import com.grafana.extensions.instrumentations.TestedInstrumentationsCustomizer;
import com.grafana.extensions.logging.LoggingExporterConfigCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import java.util.HashMap;
import java.util.Map;

public class GrafanaAutoConfigCustomizerProvider implements AutoConfigurationCustomizerProvider {

  @SuppressWarnings("deprecation")
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
        .addPropertiesSupplier(GrafanaAutoConfigCustomizerProvider::getDefaultProperties)
        .addPropertiesCustomizer(TestedInstrumentationsCustomizer::customizeProperties)
        .addPropertiesCustomizer(LoggingExporterConfigCustomizer::customizeProperties)
        .addPropertiesCustomizer(GrafanaCloudConfigCustomizer::customizeProperties)
        .addMeterProviderCustomizer(MetricFilter::dropUnusedMetrics);
  }

  private static Map<String, String> getDefaultProperties() {
    HashMap<String, String> map = new HashMap<>();
    map.put("otel.instrumentation.micrometer.base-time-unit", "s");
    map.put("otel.instrumentation.log4j-appender.experimental-log-attributes", "true");
    map.put("otel.instrumentation.logback-appender.experimental-log-attributes", "true");
    return map;
  }
}
