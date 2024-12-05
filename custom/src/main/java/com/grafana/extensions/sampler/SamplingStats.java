/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SamplingStats {

  private final Duration windowSize;
  private final Clock clock;
  private final int keepSpans;
  private final Map<String, SpanNameStats> statsMap = new ConcurrentHashMap<>();
  private final double cpuUtilizationThreshold;
  private boolean initialSampled = false;

  private final Random random = new Random(0);

  private double cpuUtilization;

  public SamplingStats(ConfigProperties properties, Clock clock) {
    // 10 slow spans per minute and operation
    // 10 random spans per minute and operation
    this.keepSpans = properties.getInt("keepSpans", 1);
    this.windowSize = properties.getDuration("window", Duration.ofMinutes(1));
    cpuUtilizationThreshold = properties.getDouble("cpuUtilizationThreshold", 0.2); // change to 0.8
    this.clock = clock;
  }

  // for testing
  public void setStats(String spanName, SpanNameStats ma) {
    this.statsMap.put(spanName, ma);
  }

  public void resetForTest() {
    statsMap.clear();
  }

  Attributes getSampledReason(ReadableSpan span) {
    String spanName = span.getName();
    long duration = span.getLatencyNanos();
    SpanData spanData = span.toSpanData();
    long startEpochNanos = spanData.getStartEpochNanos();
    // todo? is span name updated to include the route here?
    SpanNameStats stats =
        statsMap.computeIfAbsent(spanName, ma -> new SpanNameStats(windowSize, clock, keepSpans));
    boolean wasAdded = stats.add(spanData.getSpanId(), duration, startEpochNanos);

    if (!initialSampled) {
      initialSampled = true;
      return random(1.0);
    }

    if (cpuUtilization > cpuUtilizationThreshold) {
      return SampleReason.create(
          "high_cpu", Attributes.of(AttributeKey.doubleKey("cpuUtilization"), cpuUtilization));
    }

    if (!stats.isWarmedUp()) {
      return null;
    }

    Attributes topDuration = stats.isTopDuration(duration);
    if (topDuration != null) {
      return topDuration;
    }

    if (wasAdded) {
      // only roll once per span
      double randomSpanProbability = stats.isRandomSpanProbability();
      boolean roll = random.nextDouble() < randomSpanProbability;
      if (roll) {
        return random(randomSpanProbability);
      }
    }

    return null;
  }

  private static Attributes random(double randomSpanProbability) {
    return SampleReason.create(
        "random", Attributes.of(AttributeKey.doubleKey("probability"), randomSpanProbability));
  }

  public void setCpuUtilization(double cpuUtilization) {
    this.cpuUtilization = cpuUtilization;
  }
}
