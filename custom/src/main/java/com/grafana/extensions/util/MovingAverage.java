/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.util;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class MovingAverage {
  private final Queue<Long> window = new LinkedList<>();
  private final int size;
  private double sum = 0.0;

  public MovingAverage(int size) {
    this.size = size;
  }

  public static MovingAverage getPrepopulatedMovingAvgForTest(int size, int lowerBound) {
    MovingAverage ma = new MovingAverage(size);
    for (int i = 0; i < size; i++) {
      ma.add(ThreadLocalRandom.current().nextLong(lowerBound, 30_000_000));
    }
    return ma;
  }

  public int getCount() {
    return this.window.size();
  }

  public void add(long val) {
    window.offer(val);
    sum += val;
    if (!window.isEmpty() && window.size() > size) {
      sum -= window.poll();
    }
  }

  public double addAndCalcAverage(long val) {
    add(val);
    return calcAverage();
  }

  public double calcAverage() {
    if (window.isEmpty()) {
      return 0.0;
    }

    return sum / window.size();
  }
}
