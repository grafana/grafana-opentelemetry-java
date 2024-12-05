/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LatencySampler {

  private final Duration windowSize;
  private final Clock clock;
  private final int keepSpans;
  private final Map<String, SpanNameStats> movingAvgs = new ConcurrentHashMap<>();
  private boolean initialSampled = false;

  private final Random random = new Random(0);

  public LatencySampler(ConfigProperties properties, Clock clock) {
    // 10 slow spans per minute and operation
    // 10 random spans per minute and operation
    this.keepSpans = properties.getInt("keepSpans", 10);
    this.windowSize = properties.getDuration("window", Duration.ofMinutes(1));
    this.clock = clock;
  }

  // for testing
  public void setMovingAvg(String spanName, SpanNameStats ma) {
    this.movingAvgs.put(spanName, ma);
  }

  public void resetForTest() {
    movingAvgs.clear();
  }

  String getSampledReason(ReadableSpan span, String traceId) {
    String spanName = span.getName();
    long duration = span.getLatencyNanos();
    SpanData spanData = span.toSpanData();
    long startEpochNanos = spanData.getStartEpochNanos();
    // todo? is span name updated to include the route here?
    SpanNameStats average =
        movingAvgs.computeIfAbsent(
            spanName, ma -> new SpanNameStats(windowSize, clock, keepSpans));
    average.add(spanData.getSpanId(), duration, startEpochNanos);

    if (!initialSampled) {
      initialSampled = true;
      return "random";
    }
    if (!average.isWarmedUp()){
      return null;
    }

    if (average.isTopDuration(duration)) {
      return "slow";
    }

    if (random.nextDouble() < average.isRandomSpanProbability()) {
      return "random";
    }
    return null;
  }
}
