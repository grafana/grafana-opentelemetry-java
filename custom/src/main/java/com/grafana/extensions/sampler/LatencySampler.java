/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadableSpan;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LatencySampler {

  public static final Logger logger = Logger.getLogger(LatencySampler.class.getName());

  private final int windowSize;
  private final double threshold;
  private final Map<String, MovingAverage> movingAvgs = new ConcurrentHashMap<>();

  public LatencySampler(ConfigProperties properties) {
    this.threshold = properties.getDouble("threshold", 3); // 300%
    this.windowSize = properties.getInt("window", 3);
  }

  // for testing
  public void setMovingAvg(String spanName, MovingAverage ma) {
    this.movingAvgs.put(spanName, ma);
  }

  public void resetForTest() {
    movingAvgs.clear();
  }

  boolean isSlow(ReadableSpan span) {
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
