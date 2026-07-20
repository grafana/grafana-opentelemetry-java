/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MetricFilterTest {

  private static final Attributes HTTP_ATTRIBUTES =
      Attributes.builder()
          .put("http.route", "/api/test")
          .put("http.request.method", "GET")
          .put("http.response.status_code", 200)
          .put("url.scheme", "http")
          .build();

  private static final DefaultConfigProperties DATA_SAVER_ON =
      DefaultConfigProperties.createFromMap(
          Map.of(MetricFilter.APPLICATION_OBSERVABILITY_METRICS, "true"));

  @Test
  void dataSaverDisabledReturnsExporterUnchanged() {
    MetricExporter exporter = InMemoryMetricExporter.create();

    MetricExporter result =
        MetricFilter.configure(exporter, DefaultConfigProperties.createFromMap(Map.of()));

    assertThat(result).isSameAs(exporter);
  }

  @Test
  void dataSaverEnabledKeepsAllowedMetricsAndDropsOthers() {
    InMemoryMetricExporter delegate = InMemoryMetricExporter.create();
    MetricExporter exporter = MetricFilter.configure(delegate, DATA_SAVER_ON);

    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    try {
      // a default (application observability) metric
      provider
          .get("io.opentelemetry.tomcat-10.0")
          .histogramBuilder("http.server.request.duration")
          .build()
          .record(0.15, HTTP_ATTRIBUTES);
      // a metric that is not part of application observability
      provider.get("custom-lib").counterBuilder("my.custom.metric").build().add(1);
      // a manually created metric (meter named "application")
      provider.get("application").counterBuilder("my.business.metric").build().add(1);

      exporter.export(reader.collectAllMetrics());
    } finally {
      provider.close();
    }

    Set<String> exported =
        delegate.getFinishedMetricItems().stream()
            .map(MetricData::getName)
            .collect(Collectors.toSet());

    assertThat(exported)
        .contains("http.server.request.duration", "my.business.metric")
        .doesNotContain("my.custom.metric");
  }

  @Test
  void dataSaverPreservesAttributesAndAggregation() {
    // Exponential histogram is only applied when no View shadows the DefaultAggregationSelector.
    InMemoryMetricReader reader =
        InMemoryMetricReader.builder()
            .setDefaultAggregationSelector(
                DefaultAggregationSelector.getDefault()
                    .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()))
            .build();

    InMemoryMetricExporter delegate = InMemoryMetricExporter.create();
    MetricExporter exporter = MetricFilter.configure(delegate, DATA_SAVER_ON);

    SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    try {
      Meter meter = provider.get("io.opentelemetry.tomcat-10.0");
      DoubleHistogram histogram = meter.histogramBuilder("http.server.request.duration").build();
      histogram.record(0.15, HTTP_ATTRIBUTES);

      exporter.export(reader.collectAllMetrics());
    } finally {
      provider.close();
    }

    MetricData metric =
        delegate.getFinishedMetricItems().stream()
            .filter(m -> m.getName().equals("http.server.request.duration"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("http.server.request.duration was dropped"));

    assertThat(metric.getType())
        .as("Filtering at export time must not shadow the DefaultAggregationSelector")
        .isEqualTo(MetricDataType.EXPONENTIAL_HISTOGRAM);

    Set<String> attributeKeys =
        metric.getExponentialHistogramData().getPoints().stream()
            .flatMap(p -> p.getAttributes().asMap().keySet().stream())
            .map(AttributeKey::getKey)
            .collect(Collectors.toSet());
    assertThat(attributeKeys)
        .as("Filtering at export time must not alter the attribute set")
        .containsExactlyInAnyOrder(
            "http.route", "http.request.method", "http.response.status_code", "url.scheme");
  }

  @Test
  void emptyBatchDoesNotReachDelegate() {
    InMemoryMetricExporter delegate = InMemoryMetricExporter.create();
    MetricExporter exporter = MetricFilter.configure(delegate, DATA_SAVER_ON);

    InMemoryMetricReader reader = InMemoryMetricReader.create();
    SdkMeterProvider provider = SdkMeterProvider.builder().registerMetricReader(reader).build();
    try {
      LongCounter counter = provider.get("custom-lib").counterBuilder("my.custom.metric").build();
      counter.add(1);

      Collection<MetricData> metrics = reader.collectAllMetrics();
      assertThat(exporter.export(metrics).join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
    } finally {
      provider.close();
    }

    assertThat(delegate.getFinishedMetricItems()).isEmpty();
  }
}
