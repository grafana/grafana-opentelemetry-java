/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

public class MetricFilter {

  public static final String APPLICATION_OBSERVABILITY_METRICS =
      "grafana.otel.application-observability-metrics";

  static final String APPLICATION_METER_NAME = "application";

  private MetricFilter() {}

  /**
   * Wraps {@code exporter} so that, when Data Saver is enabled, only Application Observability
   * metrics are exported and everything else is dropped.
   *
   * <p>Filtering happens at export time rather than through metric {@code View}s on purpose: an
   * explicitly registered View shadows the instrument's attribute advice and the {@code
   * MetricReader}'s {@code DefaultAggregationSelector}. Filtering the exported batch instead leaves
   * retained instruments (notably {@code http.server.request.duration}) with their upstream bounded
   * attribute set and default aggregation intact.
   */
  public static MetricExporter configure(MetricExporter exporter, ConfigProperties properties) {
    if (properties.getBoolean(APPLICATION_OBSERVABILITY_METRICS, false)) {
      return new FilteringMetricExporter(exporter);
    }
    return exporter;
  }

  static boolean isAllowed(MetricData metric) {
    // allow all manually created metrics
    if (APPLICATION_METER_NAME.equals(metric.getInstrumentationScopeInfo().getName())) {
      return true;
    }
    // allow default metrics
    return DefaultMetrics.DEFAULT_METRICS.contains(metric.getName());
  }
}
