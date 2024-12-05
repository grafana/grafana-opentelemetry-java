/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.opentelemetry.api.common.Attributes;
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

    long nanosPerSecond = 1_000_000_000;

    stats.add("1", 5 * nanosPerSecond, 0);
    // should replace the previous value
    stats.add("1", 20 * nanosPerSecond, 0);

    stats.add("2", 10 * nanosPerSecond, 0);
    stats.add("3", 30 * nanosPerSecond, 0);
    // 2000 and 3000 are the top values now
    assertThat(stats.isTopDuration(20 * nanosPerSecond))
        .isEqualTo(
            Attributes.builder().put("sampled.reason", "slow").put("threshold", 20.0).build());

    assertThat(stats.isTopDuration(19 * nanosPerSecond)).isNull();
    assertThat(stats.isRandomSpanProbability()).isEqualTo(2.0 / 3.0, within(.1));

    clock.plus(Duration.ofSeconds(31));
    assertThat(stats.isWarmedUp()).isTrue();
    // not pruned yet
    assertThat(stats.isTopDuration(19 * nanosPerSecond)).isNull();

    stats.add("4", 5 * nanosPerSecond, 0);
    clock.plus(Duration.ofSeconds(31));

    // pruned
    assertThat(stats.isTopDuration(19 * nanosPerSecond))
        .isEqualTo(
            Attributes.builder().put("sampled.reason", "slow").put("threshold", 5.0).build());
  }
}
