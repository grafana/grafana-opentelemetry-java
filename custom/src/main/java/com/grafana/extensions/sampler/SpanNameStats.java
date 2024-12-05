/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class SpanNameStats {
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
  private final long sizeNanos;
  private final Clock clock;
  private final double keepSpans;
  private final Instant warmedUp;

  public SpanNameStats(Duration size, Clock clock, int keepSpans) {
    this.sizeNanos = size.toNanos();
    this.clock = clock;
    this.keepSpans = keepSpans;
    this.warmedUp = clock.instant().plus(size);
  }

  public static SpanNameStats getPrepopulatedForTest(
      Duration size, int lowerBound) {
    SpanNameStats ma = new SpanNameStats(size, Clock.systemUTC(), 10);
    ma.add("id", ThreadLocalRandom.current().nextLong(lowerBound, 30_000_000), 0);
    return ma;
  }

  public boolean add(String spanId, long durationNanos, long startEpochNanos) {
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
    while (!durations.isEmpty() && durations.peek().startEpochNanos < now - sizeNanos) {
      durations.poll();
    }
    topDurations.removeIf(e -> e.startEpochNanos < now - sizeNanos);

    addTopDuration(entry);
    return true;
  }

  private void addTopDuration(Entry entry) {
    if (topDurations.size() < keepSpans) {
      topDurations.add(entry);
      return;
    }

    topDurations.sort((a, b) -> -Long.compare(b.durationNanos, a.durationNanos));

    if (topDurations.get(0).durationNanos < entry.durationNanos) {
      topDurations.set(0, entry);
    }
  }

  public boolean isTopDuration(long durationNanos) {
    return topDurations.stream().anyMatch(e -> e.durationNanos <= durationNanos);
  }

  public double isRandomSpanProbability() {
    // want 10 per minute
    // 100 in last minute
    // probability of 0.1
    return keepSpans / durations.size();
  }

  public boolean isWarmedUp() {
    return clock.instant().isAfter(warmedUp);
  }
}
