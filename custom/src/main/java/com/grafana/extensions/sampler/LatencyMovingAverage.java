/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class LatencyMovingAverage {
  private final Queue<Long> durations = new LinkedList<>();
  private final Queue<Long> startTimes = new LinkedList<>();
  private final long sizeNanos;
  private final Clock clock;
  private final Instant warmedUp;
  private double sum = 0.0;

  public LatencyMovingAverage(Duration size, Clock clock) {
    this.sizeNanos = size.toNanos();
    this.clock = clock;
    this.warmedUp = clock.instant().plus(size);
  }

  public static LatencyMovingAverage getPrepopulatedMovingAvgForTest(
      Duration size, int lowerBound) {
    LatencyMovingAverage ma = new LatencyMovingAverage(size, Clock.systemUTC());
    ma.add(ThreadLocalRandom.current().nextLong(lowerBound, 30_000_000), 0);
    return ma;
  }

  public void add(long durationNanos, long startEpochNanos) {
    durations.offer(durationNanos);
    startTimes.offer(startEpochNanos);
    sum += durationNanos;
    long now = clock.millis() * 1_000_000;
    while (!startTimes.isEmpty() && startTimes.peek() < now - sizeNanos) {
      sum -= durations.poll();
      startTimes.poll();
    }
  }

  public double calcAverage() {
    if (durations.isEmpty()) {
      return 0.0;
    }

    return sum / durations.size();
  }

  public boolean isWarmedUp() {
    return clock.instant().isAfter(warmedUp);
  }
}
