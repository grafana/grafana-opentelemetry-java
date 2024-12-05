/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynamicSampler {
  private static final AttributeKey<String> EXCEPTION = AttributeKey.stringKey("exception.type");
  private static final AttributeKey<String> ERROR = AttributeKey.stringKey("error.type");
  private static final AttributeKey<Boolean> SAMPLED = AttributeKey.booleanKey("sampled");
  private static final AttributeKey<String> REASON = AttributeKey.stringKey("sampled.reason");
  private final Map<String, String> sampledTraces = new ConcurrentHashMap<>();
  public static final Logger logger = Logger.getLogger(DynamicSampler.class.getName());
  private final int windowSize;
  private final double threshold;
  private final Map<String, MovingAverage> movingAvgs = new ConcurrentHashMap<>();
  private static DynamicSampler INSTANCE;

  private DynamicSampler(ConfigProperties properties) {
    // read properties and configure dynamic sampling
    this.threshold = properties.getDouble("threshold", 3); // 300%
    this.windowSize = properties.getInt("window", 3);
  }

  public static void configure(ConfigProperties properties) {
    INSTANCE = new DynamicSampler(properties);
  }

  public static DynamicSampler getInstance() {
    return INSTANCE;
  }

  public void setSampled(String traceId, String reason) {
    sampledTraces.put(traceId, reason);
  }

  // for testing
  public void setMovingAvg(String spanName, MovingAverage ma) {
    this.movingAvgs.put(spanName, ma);
  }

  boolean isSampled(String traceId) {
    return sampledTraces.containsKey(traceId);
  }

  public boolean evaluateSampled(ReadWriteSpan span) {
    String traceId = span.getSpanContext().getTraceId();
    String reason = evaluateReason(span, traceId);
    if (reason != null) {
      // we might not have set the reason earlier
      span.setAttribute(REASON, reason);
      setSampled(traceId, reason);
    }
    return reason != null;
  }

  private String evaluateReason(ReadWriteSpan span, String traceId) {
    String reason = sampledTraces.get(traceId);
    if (reason != null) {
      return reason;
    }
    if (checkSampled(SAMPLED, span)) {
      return "manual";
    }
    if (hasError(span)) {
      return "error";
    }
    if (isSlow(span)) {
      return "slow";
    }
    return null;
  }

  // public visible for testing
  public void clear() {
    sampledTraces.clear();
  }

  // visible for testing
  public Set<String> getSampledTraces() {
    return Collections.unmodifiableSet(sampledTraces.keySet());
  }

  private boolean hasError(ReadableSpan span) {
    return span.toSpanData().getStatus().getStatusCode() == StatusCode.ERROR
        || checkSampled(EXCEPTION, span)
        || checkSampled(ERROR, span);
  }

  private static boolean checkSampled(AttributeKey<?> key, ReadableSpan span) {
    boolean sample;
    if (key.getType() == AttributeType.BOOLEAN) {
      sample = Boolean.TRUE.equals(span.getAttributes().get(key));
    } else {
      sample = span.getAttributes().get(key) != null;
    }
    if (sample) {
      logger.log(
          Level.INFO,
          "sending span part of Trace: {0} - due to {1}",
          new Object[] {span.toSpanData().getTraceId(), key.getKey()});
    }
    return sample;
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
