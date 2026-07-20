/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link MetricExporter} that only forwards Application Observability metrics to the delegate and
 * drops everything else. See {@link MetricFilter} for why filtering happens here rather than through
 * metric {@code View}s.
 */
class FilteringMetricExporter implements MetricExporter {

  private final MetricExporter delegate;

  FilteringMetricExporter(MetricExporter delegate) {
    this.delegate = delegate;
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    List<MetricData> filtered =
        metrics.stream().filter(MetricFilter::isAllowed).collect(Collectors.toList());
    if (filtered.isEmpty()) {
      return CompletableResultCode.ofSuccess();
    }
    return delegate.export(filtered);
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return delegate.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return delegate.getDefaultAggregation(instrumentType);
  }

  @Override
  public MemoryMode getMemoryMode() {
    return delegate.getMemoryMode();
  }

  @Override
  public void close() {
    delegate.close();
  }
}
