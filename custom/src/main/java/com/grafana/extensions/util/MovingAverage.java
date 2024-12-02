/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.util;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {
  private final Queue<Long> window = new LinkedList<>();
  private final int size;
  private double sum = 0.0;

  public MovingAverage(int size) {
    this.size = size;
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
