/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import java.time.Clock;
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
  private final Map<String, Map<String, ReadableSpan>> spansByTrace = new ConcurrentHashMap<>();
  private final Map<String, Runnable> firstSampledCallback = new ConcurrentHashMap<>();

  public static final Logger logger = Logger.getLogger(DynamicSampler.class.getName());

  private final LatencySampler latencySampler;

  private static DynamicSampler INSTANCE;

  private DynamicSampler(ConfigProperties properties, Clock clock) {
    // read properties and configure dynamic sampling
    this.latencySampler = new LatencySampler(properties, clock);
  }

  public static void configure(ConfigProperties properties, Clock clock) {
    INSTANCE = new DynamicSampler(properties, clock);
  }

  public static DynamicSampler getInstance() {
    return INSTANCE;
  }

  public void registerNewSpan(ReadableSpan span) {
    spansByTrace
        .computeIfAbsent(span.getSpanContext().getTraceId(), k -> new ConcurrentHashMap<>())
        .put(span.getSpanContext().getSpanId(), span);
  }

  public void setSampled(String traceId, String reason) {
    sampledTraces.put(traceId, reason);
  }

  // for testing
  public void setMovingAvg(String spanName, LatencyMovingAverage ma) {
    latencySampler.setMovingAvg(spanName, ma);
  }

  boolean isSampled(String traceId) {
    return sampledTraces.containsKey(traceId);
  }

  public boolean evaluateSampled(ReadWriteSpan span) {
    String traceId = span.getSpanContext().getTraceId();
    String firstReason = getFirstReason(span, traceId);
    if (firstReason != null) {
      span.setAttribute(REASON, firstReason);
      setSampled(traceId, firstReason);
      Runnable callback = firstSampledCallback.remove(traceId);
      if (callback != null) {
        callback.run();
      }
      return true;
    }
    return false;
  }

  private String getFirstReason(ReadableSpan span, String traceId) {
    String own = evaluateReason(span, traceId);
    if (own != null) {
      return own;
    }
    for (ReadableSpan anySpan : spansByTrace.get(traceId).values()) {
      String reason = evaluateReason(anySpan, traceId);
      if (reason != null) {
        return reason;
      }
    }
    return null;
  }

  private String evaluateReason(ReadableSpan span, String traceId) {
    String reason = sampledTraces.get(traceId);
    if (reason != null) {
      return reason;
    }
    if (checkSampled(SAMPLED, span, traceId)) {
      return "manual";
    }
    if (hasError(span, traceId)) {
      return "error";
    }
    if (latencySampler.isSlow(span, traceId)) {
      return "slow";
    }
    return null;
  }

  public void resetForTest() {
    sampledTraces.clear();
    spansByTrace.clear();
    latencySampler.resetForTest();
    firstSampledCallback.clear();
  }

  void removeTrace(String traceId) {
    sampledTraces.remove(traceId);
    spansByTrace.remove(traceId);
  }

  // visible for testing
  public Set<String> getSampledTraces() {
    return Collections.unmodifiableSet(sampledTraces.keySet());
  }

  private boolean hasError(ReadableSpan span, String traceId) {
    return span.toSpanData().getStatus().getStatusCode() == StatusCode.ERROR
        || checkSampled(EXCEPTION, span, traceId)
        || checkSampled(ERROR, span, traceId);
  }

  private static boolean checkSampled(AttributeKey<?> key, ReadableSpan span, String traceId) {
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
          new Object[] {traceId, key.getKey()});
    }
    return sample;
  }

  public void registerOnFirstSampledCallback(Context context, Runnable runnable) {
    firstSampledCallback.put(Span.fromContext(context).getSpanContext().getTraceId(), runnable);
  }
}
