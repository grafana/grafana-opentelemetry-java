/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SamplingStats {

  private final Duration windowSize;
  private final Clock clock;
  private final int keepSpans;
  private final Map<String, OperationStats> statsMap = new ConcurrentHashMap<>();
  private final HighCpuDetector highCpuDetector;

  public SamplingStats(ConfigProperties properties, Clock clock) {
    this.keepSpans = properties.getInt("keepSpans", 3);
    this.windowSize = properties.getDuration("window", Duration.ofMinutes(1));
    // change to 0.8
    this.highCpuDetector =
        new HighCpuDetector(properties.getDouble("cpuUtilizationThreshold", 0.6));
    this.clock = clock;
  }

  // for testing
  public void setStats(String spanName, OperationStats ma) {
    this.statsMap.put(spanName, ma);
  }

  public void resetForTest() {
    statsMap.clear();
  }

  Attributes getSampledReason(ReadableSpan span) {
    String spanName = span.getName();
    SpanData spanData = span.toSpanData();
    // todo? is span name updated to include the route here?
    OperationStats stats =
        statsMap.computeIfAbsent(
            spanName, ma -> new OperationStats(spanName, windowSize, clock, keepSpans));
    return stats.getSampledReason(spanData, span.getLatencyNanos(), highCpuDetector);
  }

  public void setCpuUtilization(double cpuUtilization) {
    highCpuDetector.setCpuUtilization(cpuUtilization);
  }
}
