/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;

public class SamplerMetricExporter implements MetricExporter {
  private final MetricExporter delegate;

  public SamplerMetricExporter(MetricExporter delegate) {
    this.delegate = delegate;
  }

  public static MetricExporter configure(
      MetricExporter metricExporter, ConfigProperties configProperties) {
    return new SamplerMetricExporter(metricExporter);
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
  public CompletableResultCode export(Collection<MetricData> metrics) {
    for (MetricData metric : metrics) {
      if (metric.getName().equals("jvm.system.cpu.utilization")) {
        metric
            .getDoubleGaugeData()
            .getPoints()
            .forEach(point -> DynamicSampler.getInstance().setCpuUtilization(point.getValue()));
      }
    }
    return delegate.export(metrics);
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
  public void close() {
    delegate.close();
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return delegate.getAggregationTemporality(instrumentType);
  }
}
