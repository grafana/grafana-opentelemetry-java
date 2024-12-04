/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
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
  private static final AttributeKey<String> EXCEPTION = AttributeKey.stringKey("exception.type");
  private static final AttributeKey<String> ERROR = AttributeKey.stringKey("error.type");
  private final Set<String> sampledTraces = new ConcurrentSkipListSet<>();
  public static final Logger logger = Logger.getLogger(DynamicSampler.class.getName());
  private final int windowSize;
  private final double threshold;
  private final Map<String, MovingAverage> movingAvgs = new ConcurrentHashMap<>();
  private static DynamicSampler INSTANCE;

  private DynamicSampler(ConfigProperties properties) {
    // read properties and configure dynamic sampling
    this.threshold = properties.getDouble("threshold", 1.3); // 30%
    this.windowSize = properties.getInt("window", 3);
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

  // for testing
  public void setMovingAvg(String spanName, MovingAverage ma) {
    this.movingAvgs.put(spanName, ma);
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
    return isSlow(span) || hasError(span);
  }

  private boolean hasError(ReadableSpan span) {
    return span.toSpanData().getStatus().getStatusCode() == StatusCode.ERROR
        || span.getAttributes().get(EXCEPTION) != null
        || span.getAttributes().get(ERROR) != null;
  }

  private boolean isSlow(ReadableSpan span) {
    String spanName = span.getName();
    logger.log(
        Level.INFO,
        "spanName {0} - windowSize {1}: {2}",
        new Object[] {span.getName(), windowSize, span.getAttributes()});
    long duration = span.getLatencyNanos();
    MovingAverage currMovingAvg =
        movingAvgs.computeIfAbsent(spanName, ma -> new MovingAverage(windowSize));
    currMovingAvg.add(duration);
    if (currMovingAvg.getCount() < windowSize) {
      return false;
    }
    double avg = currMovingAvg.calcAverage();
    logger.log(
        Level.INFO,
        "avg {0} * threshold {1} = {2}, duration {3}",
        new Object[] {avg, threshold, avg * threshold, duration});
    // discard
    if (duration < avg * threshold) {
      return false;
    }
    logger.log(
        Level.INFO,
        "sending span part of Trace: {0} - {1}",
        new Object[] {span.toSpanData().getTraceId(), duration});
    return true;
  }
}
