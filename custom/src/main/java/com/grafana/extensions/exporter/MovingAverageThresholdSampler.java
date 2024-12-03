/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.exporter;

import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovingAverageThresholdSampler implements SpanExporter {
  private final Map<String, MovingAverage> movingAvgs = new ConcurrentHashMap<>();
  private final double thresholdVal;
  private final int windowSize;

  public static final Logger logger =
      Logger.getLogger(MovingAverageThresholdSampler.class.getName());
  private final SpanExporter delegate;

  protected MovingAverageThresholdSampler(
      double thresholdVal, int windowSize, SpanExporter delegate) {
    this.delegate = delegate;
    this.thresholdVal = 0.5;
    this.windowSize = windowSize;
  }

  public static SpanExporter configure(SpanExporter delegate, ConfigProperties properties) {
    double threshold = properties.getDouble("threshold", 1.5);
    int windowSize = properties.getInt("window", 5);
    return new MovingAverageThresholdSampler(threshold, windowSize, delegate);
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    Iterator<SpanData> iter = spans.iterator();
    while (iter.hasNext()) {
      SpanData span = iter.next();
      String spanName = span.getName();
      logger.log(
          Level.INFO,
          "exp:spanName {0} - windowSize {1}: {2}",
          new Object[] {span.getName(), windowSize, span.getAttributes()});
      long duration = (span.getEndEpochNanos() - span.getStartEpochNanos()) / 1_000_000;
      MovingAverage currMovingAvg =
          movingAvgs.computeIfAbsent(spanName, ma -> new MovingAverage(windowSize));
      currMovingAvg.add(duration);
      if (currMovingAvg.getCount() >= windowSize) {
        double avg = currMovingAvg.calcAverage();
        logger.log(
            Level.INFO,
            "exp: avg {0} * threshold {1} = {2}, duration {3}",
            new Object[] {avg, thresholdVal, avg * thresholdVal, duration});
        if (duration < avg * thresholdVal) {
          logger.log(Level.INFO, "discarding: " + span.getName());
          spans.remove(span);
        }
      }
      logger.log(Level.INFO, "exporting span:{0}", new Object[] {spans.size()});
    }
    return delegate.export(spans);
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
