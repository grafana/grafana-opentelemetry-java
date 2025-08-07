/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;

public class MetricFilter {

  public static final String APPLICATION_OBSERVABILITY_METRICS =
      "grafana.otel.application-observability-metrics";

  private MetricFilter() {}

  static void dropUnusedMetrics(SdkMeterProviderBuilder sdkMeterProviderBuilder) {
    View allow = View.builder().build();

    // allow all manually created metrics
    sdkMeterProviderBuilder.registerView(
        InstrumentSelector.builder().setMeterName("application").build(), allow);

    // allow default metrics
    for (String metric : DefaultMetrics.DEFAULT_METRICS) {
      sdkMeterProviderBuilder.registerView(
          InstrumentSelector.builder().setName(metric).build(), allow);
    }

    // drop everything else
    sdkMeterProviderBuilder.registerView(
        InstrumentSelector.builder().setName("*").build(),
        View.builder().setAggregation(Aggregation.drop()).build());
  }

  static void configure(
      SdkMeterProviderBuilder sdkMeterProviderBuilder, ConfigProperties properties) {
    if (properties.getBoolean(APPLICATION_OBSERVABILITY_METRICS, false)) {
      dropUnusedMetrics(sdkMeterProviderBuilder);
    }
  }
}
