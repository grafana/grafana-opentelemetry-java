/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class OperationStats {

  private static class Entry {
    String spanId;
    Long durationNanos;
    Long startEpochNanos;

    public Entry(Long durationNanos, Long startEpochNanos, String spanId) {
      this.durationNanos = durationNanos;
      this.startEpochNanos = startEpochNanos;
      this.spanId = spanId;
    }
  }

  private final Queue<Entry> durations = new LinkedList<>();
  private final List<Entry> topDurations = new LinkedList<>();
  private final String spanName; // just for debugging
  private final long windowNanos;
  private final Clock clock;
  private final double keepSpans;
  private final Instant start;
  private final Random random = new Random(0);
  private final Instant warmedUp;
  private int initialSpansLeft;

  public OperationStats(String spanName, Duration window, Clock clock, int keepSpans) {
    this.spanName = spanName;
    this.windowNanos = window.toNanos();
    this.clock = clock;
    this.keepSpans = keepSpans;
    initialSpansLeft = keepSpans;
    this.start = clock.instant();
    this.warmedUp = clock.instant().plus(window);
  }

  public static OperationStats getPrepopulatedForTest(Duration size, int lowerBound) {
    OperationStats ma = new OperationStats("test", size, Clock.systemUTC(), 10);
    ma.add("id", ThreadLocalRandom.current().nextLong(lowerBound, 30_000_000), 0);
    return ma;
  }

  Attributes getSampledReason(SpanData spanData, long duration, HighCpuDetector highCpuDetector) {
    boolean wasAdded = add(spanData.getSpanId(), duration, spanData.getStartEpochNanos());

    Attributes initial = getInitial();
    if (initial != null) {
      return initial;
    }

    Attributes highCpu = highCpuDetector.getSampledReason();
    if (highCpu != null) {
      return highCpu;
    }

    if (!isWarmedUp()) {
      return null;
    }

    Attributes slow = getSlow(duration);
    if (slow != null) {
      return slow;
    }

    if (wasAdded) {
      // only roll once per span
      double randomSpanProbability = isRandomSpanProbability();
      boolean roll = random.nextDouble() < randomSpanProbability;
      if (roll) {
        return SampleReason.create(
            "random", Attributes.of(AttributeKey.doubleKey("probability"), randomSpanProbability));
      }
    }

    return null;
  }

  boolean add(String spanId, long durationNanos, long startEpochNanos) {
    for (Entry entry : durations) {
      if (entry.spanId.equals(spanId)) {
        entry.durationNanos = durationNanos;

        // we might now be in the topDurations because of the new duration when the span is ended
        topDurations.remove(entry);
        addTopDuration(entry);
        return false;
      }
    }
    Entry entry = new Entry(durationNanos, startEpochNanos, spanId);
    durations.offer(entry);

    long now = clock.millis() * 1_000_000;
    while (!durations.isEmpty() && durations.peek().startEpochNanos < now - windowNanos) {
      durations.poll();
    }
    topDurations.removeIf(e -> e.startEpochNanos < now - windowNanos);

    addTopDuration(entry);
    return true;
  }

  private void addTopDuration(Entry entry) {
    if (topDurations.size() < keepSpans) {
      topDurations.add(entry);
      return;
    }

    sortTopDurations();

    if (topDurations.get(0).durationNanos < entry.durationNanos) {
      topDurations.set(0, entry);
    }
  }

  private void sortTopDurations() {
    topDurations.sort(Comparator.comparingLong(a -> a.durationNanos));
  }

  Attributes getSlow(long durationNanos) {
    sortTopDurations();

    Long threshold = topDurations.get(0).durationNanos;
    boolean b = threshold <= durationNanos;
    return b
        ? SampleReason.create(
            "slow", Attributes.of(AttributeKey.doubleKey("threshold"), (double) threshold / 1e9))
        : null;
  }

  double isRandomSpanProbability() {
    if (durations.isEmpty()) {
      return 1.0;
    }
    // want 10 per minute
    // 100 in last minute
    // probability of 0.1
    return Math.min(1, keepSpans / durations.size());
  }

  Attributes getInitial() {
    if (initialSpansLeft == 0) {
      return null;
    }
    double haveRatio = (double) start.until(clock.instant(), ChronoUnit.NANOS) / windowNanos;
    double wantRatio = 1.0 - (double) initialSpansLeft / keepSpans;

    if (haveRatio >= wantRatio) {
      initialSpansLeft--;
      return SampleReason.create("initial");
    }
    return null;
  }

  // visible for testing
  boolean isWarmedUp() {
    return clock.instant().isAfter(warmedUp);
  }
}
