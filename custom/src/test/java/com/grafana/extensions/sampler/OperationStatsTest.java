/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spock.util.time.MutableClock;

class OperationStatsTest {

  MutableClock clock = new MutableClock();
  OperationStats stats;
  long nanosPerSecond = 1_000_000_000;

  @BeforeEach
  void setUp() {
    clock.setInstant(Instant.EPOCH);
    stats = new OperationStats("test", Duration.ofMinutes(1), clock, 2);
  }

  @Test
  void initial() {
    // get 1 span in the first 30 seconds
    assertThat(stats.getInitial())
        .isEqualTo(Attributes.of(AttributeKey.stringKey("sampled.reason"), "initial"));
    assertThat(stats.getInitial()).isNull();

    clock.plus(Duration.ofSeconds(29));
    assertThat(stats.getInitial()).isNull();
    clock.plus(Duration.ofSeconds(1));
    assertThat(stats.getInitial())
        .isEqualTo(Attributes.of(AttributeKey.stringKey("sampled.reason"), "initial"));

    // all initial are depleted - waiting will not help
    clock.plus(Duration.ofSeconds(100));
    assertThat(stats.getInitial()).isNull();
  }

  @Test
  void initialAllAtOnce() {
    clock.plus(Duration.ofSeconds(30));
    assertThat(stats.getInitial())
        .isEqualTo(Attributes.of(AttributeKey.stringKey("sampled.reason"), "initial"));
    assertThat(stats.getInitial())
        .isEqualTo(Attributes.of(AttributeKey.stringKey("sampled.reason"), "initial"));

    // all initial are depleted - waiting will not help
    clock.plus(Duration.ofSeconds(100));
    assertThat(stats.getInitial()).isNull();
  }

  @Test
  void slowAndRandom() {
    clock.plus(Duration.ofSeconds(30));

    stats.add("1", toNanos(5), 0);
    assertThat(stats.isRandomSpanProbability()).isOne();
    // should replace the previous value
    stats.add("1", toNanos(20), 0);

    stats.add("2", toNanos(10), 0);
    assertThat(stats.isRandomSpanProbability()).isOne();

    stats.add("3", toNanos(30), 0);
    // 2000 and 3000 are the top values now
    assertThat(stats.getSlow(toNanos(20)))
        .isEqualTo(
            Attributes.builder().put("sampled.reason", "slow").put("threshold", 20.0).build());

    assertThat(stats.getSlow(toNanos(19))).isNull();
    assertThat(stats.isRandomSpanProbability()).isEqualTo(2.0 / 3.0, within(.1));
    assertThat(stats.isWarmedUp()).isFalse();

    clock.plus(Duration.ofSeconds(31));
    assertThat(stats.isWarmedUp()).isTrue();

    // not pruned yet
    assertThat(stats.getSlow(toNanos(19))).isNull();

    stats.add("4", toNanos(5), 0);
    clock.plus(Duration.ofSeconds(31));

    // pruned
    assertThat(stats.getSlow(toNanos(19)))
        .isEqualTo(
            Attributes.builder().put("sampled.reason", "slow").put("threshold", 5.0).build());
  }

  private long toNanos(int seconds) {
    return seconds * nanosPerSecond;
  }
}
