/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadableSpan;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynamicSampler {
  private final Set<String> sampledTraces = new ConcurrentSkipListSet<>();
  public static final Logger logger = Logger.getLogger(DynamicSampler.class.getName());
  private final int windowSize;
  private final double thresholdVal;
  private final Map<String, MovingAverage> movingAvgs = new ConcurrentHashMap<>();
  private static DynamicSampler INSTANCE;

  private DynamicSampler(ConfigProperties properties) {
    // read properties and configure dynamic sampling
    this.thresholdVal = properties.getDouble("threshold", 1.5);
    this.windowSize = properties.getInt("window", 5);
  }

  public static void configure(ConfigProperties properties) {
    INSTANCE = new DynamicSampler(properties);
  }

  public static DynamicSampler getInstance() {
    return INSTANCE;
  }

  public void setSampled(String traceId) {
    sampledTraces.add(traceId);
  }

  boolean isSampled(String traceId) {
    return sampledTraces.contains(traceId);
  }

  public boolean evaluateSampled(ReadableSpan span) {
    String traceId = span.getSpanContext().getTraceId();
    if (sampledTraces.contains(traceId)) {
      return true;
    }
    if (shouldSample(span)) {
      setSampled(traceId);
      return true;
    }
    return false;
  }

  // public visible for testing
  public void clear() {
    sampledTraces.clear();
  }

  // visible for testing
  public Set<String> getSampledTraces() {
    return Collections.unmodifiableSet(sampledTraces);
  }

  boolean shouldSample(ReadableSpan span) {
    String spanName = span.getName();
    logger.log(
        Level.INFO,
        "spanName {0} - windowSize {1}: {2}",
        new Object[] {span.getName(), windowSize, span.getAttributes()});
    long duration = (span.getLatencyNanos()) / 1_000_000;
    MovingAverage currMovingAvg =
        movingAvgs.computeIfAbsent(spanName, ma -> new MovingAverage(windowSize));
    currMovingAvg.add(duration);
    if (currMovingAvg.getCount() >= windowSize) {
      double avg = currMovingAvg.calcAverage();
      logger.log(
          Level.INFO,
          "avg {0} * threshold {1} = {2}, duration {3}",
          new Object[] {avg, thresholdVal, avg * thresholdVal, duration});
      // discard
      if (duration < avg * thresholdVal) {
        return false;
      }
    }
    return true;
  }
}
