/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

// This class is generated from README.md in custom/build.gradle.  Do not edit.

import java.util.Arrays;
import java.util.List;

public class DefaultMetrics {

  public static final List<String> DEFAULT_METRICS =
      Arrays.asList(
          "process.runtime.jvm.system.cpu.utilization",
          "process.runtime.jvm.memory.usage",
          "process.runtime.jvm.memory.limit",
          "process.runtime.jvm.gc.duration",
          "process.runtime.jvm.classes.current_loaded",
          "process.runtime.jvm.threads.count",
          "db.client.connections.usage",
          "db.client.connections.max",
          "db.client.connections.pending_requests",
          "r2dbc.pool.acquired",
          "r2dbc.pool.max.allocated",
          "r2dbc.pool.pending",
          "kafka.producer.record.error_total",
          "mongodb.driver.pool.waitqueuesize",
          "mongodb.driver.pool.checkedout");
}
