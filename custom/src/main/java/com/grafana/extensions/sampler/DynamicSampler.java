/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
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
import java.util.logging.Logger;

public class DynamicSampler {
  private static final AttributeKey<String> EXCEPTION = AttributeKey.stringKey("exception.type");
  private static final AttributeKey<String> ERROR = AttributeKey.stringKey("error.type");
  private static final AttributeKey<Boolean> SAMPLED = AttributeKey.booleanKey("sampled");

  private final Map<String, Attributes> sampledTraces = new ConcurrentHashMap<>();
  private final Map<String, Map<String, ReadableSpan>> spansByTrace = new ConcurrentHashMap<>();
  private final Map<String, Runnable> firstSampledCallback = new ConcurrentHashMap<>();

  public static final Logger logger = Logger.getLogger(DynamicSampler.class.getName());

  private final SamplingStats samplingStats;

  private static DynamicSampler INSTANCE;

  private DynamicSampler(ConfigProperties properties, Clock clock) {
    // read properties and configure dynamic sampling
    this.samplingStats = new SamplingStats(properties, clock);
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

  public void setSampled(String traceId, Attributes reason) {
    sampledTraces.put(traceId, reason);
  }

  // for testing
  public void setStats(String spanName, OperationStats ma) {
    samplingStats.setStats(spanName, ma);
  }

  boolean isSampled(String traceId) {
    return sampledTraces.containsKey(traceId);
  }

  public boolean evaluateSampled(ReadWriteSpan span) {
    String traceId = span.getSpanContext().getTraceId();
    Attributes firstReason = getFirstReason(span, traceId);
    if (firstReason != null) {
      // skip attributes that are already set - so that we can simulate the "child" reason
      Attributes add = firstReason.toBuilder().removeIf(k -> span.getAttribute(k) != null).build();
      span.setAllAttributes(add);
      setSampled(traceId, firstReason);
      Runnable callback = firstSampledCallback.remove(traceId);
      if (callback != null) {
        callback.run();
      }
      return true;
    }
    return false;
  }

  private Attributes getFirstReason(ReadableSpan span, String traceId) {
    Attributes own = evaluateReason(span, traceId);
    if (own != null) {
      return own;
    }
    for (ReadableSpan anySpan : spansByTrace.get(traceId).values()) {
      Attributes reason = evaluateReason(anySpan, traceId);
      if (reason != null) {
        return reason;
      }
    }
    return null;
  }

  private Attributes evaluateReason(ReadableSpan span, String traceId) {
    // need to add span to the moving average - even if we don't use the result
    Attributes latencySamplerSampledReason = samplingStats.getSampledReason(span);

    Attributes reason = sampledTraces.get(traceId);
    if (reason != null) {
      return reason;
    }
    if (checkSampled(SAMPLED, span) || checkSampled(SampleReason.REASON, span)) {
      return SampleReason.create("manual");
    }
    if (hasError(span, traceId)) {
      return SampleReason.create("error");
    }
    return latencySamplerSampledReason;
  }

  public void resetForTest() {
    sampledTraces.clear();
    spansByTrace.clear();
    samplingStats.resetForTest();
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
        || checkSampled(EXCEPTION, span)
        || checkSampled(ERROR, span);
  }

  private static boolean checkSampled(AttributeKey<?> key, ReadableSpan span) {
    if (key.getType() == AttributeType.BOOLEAN) {
      return Boolean.TRUE.equals(span.getAttributes().get(key));
    } else {
      return span.getAttributes().get(key) != null;
    }
  }

  public void registerOnFirstSampledCallback(Context context, Runnable runnable) {
    firstSampledCallback.put(Span.fromContext(context).getSpanContext().getTraceId(), runnable);
  }

  public void setCpuUtilization(double cpuUtilization) {
    samplingStats.setCpuUtilization(cpuUtilization);
  }
}
