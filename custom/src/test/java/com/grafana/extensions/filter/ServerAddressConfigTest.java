/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ServerAddressConfigTest {

  private static final Attributes RECORD_ATTRIBUTES =
      Attributes.builder()
          .put("http.route", "/api/test")
          .put("http.request.method", "GET")
          .put("http.response.status_code", 200)
          .put("error.type", "")
          .put("network.protocol.name", "http")
          .put("network.protocol.version", "1.1")
          .put("url.scheme", "http")
          .put("server.address", "example.com")
          .put("server.port", 8080)
          .put("extra.attribute", "should-be-dropped")
          .build();

  @Test
  void defaultConfigAllowsExponentialAggregation() {
    InMemoryMetricReader reader =
        InMemoryMetricReader.builder()
            .setDefaultAggregationSelector(
                DefaultAggregationSelector.getDefault()
                    .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()))
            .build();

    SdkMeterProviderBuilder providerBuilder =
        SdkMeterProvider.builder().registerMetricReader(reader);
    ServerAddressConfig.configure(providerBuilder, DefaultConfigProperties.createFromMap(Map.of()));

    SdkMeterProvider provider = providerBuilder.build();
    try {
      recordHistogram(provider);
      Collection<MetricData> metrics = reader.collectAllMetrics();

      MetricData metric = findMetric(metrics, "http.server.request.duration");
      assertThat(metric.getType())
          .as("Without opt-in, the DefaultAggregationSelector should apply")
          .isEqualTo(MetricDataType.EXPONENTIAL_HISTOGRAM);
    } finally {
      provider.close();
    }
  }

  @Test
  void serverAddressOptInIncludesExtraAttributes() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();

    SdkMeterProviderBuilder providerBuilder =
        SdkMeterProvider.builder().registerMetricReader(reader);
    ServerAddressConfig.configure(
        providerBuilder,
        DefaultConfigProperties.createFromMap(
            Map.of(ServerAddressConfig.SERVER_ADDRESS_OPT_IN, "true")));

    SdkMeterProvider provider = providerBuilder.build();
    try {
      recordHistogram(provider);
      Collection<MetricData> metrics = reader.collectAllMetrics();

      MetricData metric = findMetric(metrics, "http.server.request.duration");
      Set<String> attributeKeys =
          metric.getHistogramData().getPoints().stream()
              .flatMap(p -> p.getAttributes().asMap().keySet().stream())
              .map(AttributeKey::getKey)
              .collect(Collectors.toSet());

      assertThat(attributeKeys)
          .as("View should include server.address and server.port")
          .contains("server.address", "server.port");
      assertThat(attributeKeys)
          .as("View should filter out unknown attributes")
          .doesNotContain("extra.attribute");
    } finally {
      provider.close();
    }
  }

  @Test
  void serverAddressOptInShadowsExponentialAggregation() {
    InMemoryMetricReader reader =
        InMemoryMetricReader.builder()
            .setDefaultAggregationSelector(
                DefaultAggregationSelector.getDefault()
                    .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()))
            .build();

    SdkMeterProviderBuilder providerBuilder =
        SdkMeterProvider.builder().registerMetricReader(reader);
    ServerAddressConfig.configure(
        providerBuilder,
        DefaultConfigProperties.createFromMap(
            Map.of(ServerAddressConfig.SERVER_ADDRESS_OPT_IN, "true")));

    SdkMeterProvider provider = providerBuilder.build();
    try {
      recordHistogram(provider);
      Collection<MetricData> metrics = reader.collectAllMetrics();

      MetricData metric = findMetric(metrics, "http.server.request.duration");
      // When a View is registered, it shadows the DefaultAggregationSelector.
      // This test documents the known limitation: with opt-in enabled,
      // exponential aggregation is not applied.
      assertThat(metric.getType()).isEqualTo(MetricDataType.HISTOGRAM);
    } finally {
      provider.close();
    }
  }

  private static void recordHistogram(SdkMeterProvider provider) {
    Meter meter = provider.get("test-meter");
    DoubleHistogram histogram =
        meter
            .histogramBuilder("http.server.request.duration")
            .setUnit("s")
            .setDescription("Duration of HTTP server requests.")
            .build();
    histogram.record(0.15, RECORD_ATTRIBUTES);
  }

  private static MetricData findMetric(Collection<MetricData> metrics, String name) {
    return metrics.stream()
        .filter(m -> m.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Metric not found: " + name));
  }
}
