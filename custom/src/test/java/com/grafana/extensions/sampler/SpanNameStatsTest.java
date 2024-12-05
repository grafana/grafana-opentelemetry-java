/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import spock.util.time.MutableClock;

class SpanNameStatsTest {
  @Test
  void test() {
    MutableClock clock = new MutableClock();
    clock.setInstant(Instant.EPOCH);
    SpanNameStats stats = new SpanNameStats(Duration.ofMinutes(1), clock, 2);
    clock.plus(Duration.ofSeconds(30));
    assertThat(stats.isWarmedUp()).isFalse();

    stats.add("1", 500, 0);
    // should replace the previous value
    stats.add("1", 2000, 0);

    stats.add("2", 1000, 0);
    stats.add("3", 3000, 0);
    // 2000 and 3000 are the top values now
    assertThat(stats.isTopDuration(2000)).isTrue();
    assertThat(stats.isTopDuration(1999)).isFalse();
    assertThat(stats.isRandomSpanProbability()).isEqualTo(2.0 / 3.0, within(.1));

    clock.plus(Duration.ofSeconds(31));
    assertThat(stats.isWarmedUp()).isTrue();
    // not pruned yet
    assertThat(stats.isTopDuration(1999)).isFalse();

    stats.add("4", 500, 0);
    clock.plus(Duration.ofSeconds(31));

    // pruned
    assertThat(stats.isTopDuration(1999)).isTrue();
  }
}
