/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.processor;

import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovingAverageProcessor implements SpanProcessor {
  private final Map<String, MovingAverage> movingAvgs = new ConcurrentHashMap<>();
  private final double thresholdVal;
  private final int windowSize;

  public static final Logger logger = Logger.getLogger(MovingAverageProcessor.class.getName());

  protected MovingAverageProcessor(double thresholdVal, int windowSize) {
    this.thresholdVal = thresholdVal;
    this.windowSize = windowSize;

    //    exporter = OtlpProtocolPropertiesSupplier;
  }

  public static SpanProcessor configure(SpanProcessor sp, ConfigProperties properties) {
    double threshold = properties.getDouble("threshold", 1.5);
    int windowSize = properties.getInt("window", 5);
    return new MovingAverageProcessor(threshold, windowSize);
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {}

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan span) {
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
        return;
      }
    }
    logger.log(Level.INFO, "sending forward:{0}", new Object[] {span.getName()});
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }
}
