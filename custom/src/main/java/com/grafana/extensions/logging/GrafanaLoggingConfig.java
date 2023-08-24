/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.logging;

import java.util.List;

public class GrafanaLoggingConfig {

  public static final String LOGGING_ENABLED_PROP = "grafana.otlp.logging.exporter.enabled";
  public static final String DEBUG_LOGGING_PROP = "grafana.otlp.debug.logging";

  private final boolean debugLogging;
  private final List<String> loggingEnabled;

  public GrafanaLoggingConfig(boolean debugLogging, List<String> loggingEnabled) {
    this.debugLogging = debugLogging;
    this.loggingEnabled = loggingEnabled;
  }

  public boolean isDebugLogging() {
    return debugLogging;
  }

  public List<String> getLoggingEnabled() {
    return loggingEnabled;
  }
}
