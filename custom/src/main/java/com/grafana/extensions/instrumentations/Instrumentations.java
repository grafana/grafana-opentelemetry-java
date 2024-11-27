/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.instrumentations;

import java.util.Arrays;
import java.util.List;

public class Instrumentations {

  public static final List<String> TESTED_INSTRUMENTATIONS =
      Arrays.asList(
          "opentelemetry-extension-annotations",
          "opentelemetry-instrumentation-annotations",
          "opentelemetry-api",
          "tomcat",
          "jetty",
          "netty",
          "undertow",
          "spring-web",
          "spring-webmvc",
          "spring-webflux",
          "reactor",
          "spring-data",
          "jdbc",
          "hikaricp",
          "r2dbc",
          "jms",
          "logback-appender",
          "log4j-appender",
          "runtime-telemetry",
          "executors",
          "micrometer",
          "kafka-clients",
          "spring-kafka",
          "mongo",
          "jedis",
          "lettuce");
}
