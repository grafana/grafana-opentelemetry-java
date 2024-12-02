/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampling;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MovingAverageThresholdSampler implements Sampler {
  private final Map<String, MovingAverage> movingAvgs = new ConcurrentHashMap<>();
  private final double thresholdVal;
  private final int windowSize;

  protected MovingAverageThresholdSampler(double thresholdVal, int windowSize) {
    this.thresholdVal = thresholdVal;
    this.windowSize = windowSize;
  }

  public static Sampler configure(Sampler sampler, ConfigProperties properties) {
    double threshold = properties.getDouble("threshold", 1.5);
    int windowSize = properties.getInt("window", 20);
    return new MovingAverageThresholdSampler(threshold, windowSize);
  }

  @Override
  public SamplingResult shouldSample(
      Context context,
      String traceId,
      String spanName,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> list) {
    Long duration = attributes.get(AttributeKey.longKey("duration"));
    if (duration == null) {
      return SamplingResult.recordAndSample();
    }

    MovingAverage currMovingAvg =
        movingAvgs.computeIfAbsent(spanName, ma -> new MovingAverage(windowSize));
    // record until window is full
    if (currMovingAvg.getCount() < windowSize) {
      currMovingAvg.addAndCalcAverage(duration);
      return SamplingResult.recordAndSample();
    }

    double avg = currMovingAvg.addAndCalcAverage(duration);
    if (duration < avg * thresholdVal) {
      return SamplingResult.drop();
    }

    return SamplingResult.recordAndSample();
  }

  @Override
  public String getDescription() {
    return "MovingAverageThresholdSampler";
  }
}
