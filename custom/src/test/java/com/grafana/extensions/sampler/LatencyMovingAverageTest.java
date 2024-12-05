/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import spock.util.time.MutableClock;

class LatencyMovingAverageTest {
  @Test
  void test() {
    MutableClock clock = new MutableClock();
    clock.setInstant(Instant.EPOCH);
    LatencyMovingAverage latencyMovingAverage =
        new LatencyMovingAverage(Duration.ofMinutes(1), clock);
    clock.plus(Duration.ofSeconds(30));
    assertThat(latencyMovingAverage.isWarmedUp()).isFalse();

    latencyMovingAverage.add(1000, 0);
    latencyMovingAverage.add(2000, 0);
    assertThat(latencyMovingAverage.calcAverage()).isEqualTo(1500.0);

    clock.plus(Duration.ofSeconds(31));
    assertThat(latencyMovingAverage.isWarmedUp()).isTrue();

    latencyMovingAverage.add(3000, Instant.ofEpochSecond(61).toEpochMilli() * 1_000_000);
    assertThat(latencyMovingAverage.calcAverage()).isEqualTo(3000.0);
  }
}
