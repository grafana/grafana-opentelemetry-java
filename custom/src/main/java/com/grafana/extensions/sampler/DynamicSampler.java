/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadableSpan;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class DynamicSampler {
  private static final Set<String> sampledTraces = new ConcurrentSkipListSet<>();

  public DynamicSampler(ConfigProperties properties) {
    // read properties and configure dynamic sampling
  }

  public static void setSampled(String traceId) {
    sampledTraces.add(traceId);
  }

  static boolean isSampled(String traceId) {
    return sampledTraces.contains(traceId);
  }

  public static boolean evaluateSampled(ReadableSpan span) {
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
  public static void clear() {
    sampledTraces.clear();
  }

  // visible for testing
  public static Set<String> getSampledTraces() {
    return Collections.unmodifiableSet(sampledTraces);
  }

  static boolean shouldSample(ReadableSpan span) {
    // add dynamic sampling logic here
    // dummy implementation for testing for now
    return Boolean.TRUE.equals(span.getAttributes().get(AttributeKey.booleanKey("sampled")));
  }
}
