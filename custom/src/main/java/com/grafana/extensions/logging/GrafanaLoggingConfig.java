/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.logging;

import com.grafana.extensions.util.StringUtils;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrafanaLoggingConfig {

  public static final String LOGGING_ENABLED_PROP = "grafana.otlp.logging.exporter.enabled";
  public static final String DEBUG_LOGGING_PROP = "grafana.otlp.debug.logging";

  private final boolean debugLogging;
  private final List<String> loggingEnabled;

  public GrafanaLoggingConfig(ConfigProperties configs) {
    this.debugLogging = configs.getBoolean(DEBUG_LOGGING_PROP, false);
    String loggingEnabled = configs.getString(LOGGING_ENABLED_PROP, "");
    this.loggingEnabled =
        StringUtils.isNotBlank(loggingEnabled)
            ? Arrays.asList(loggingEnabled.split(","))
            : new ArrayList<>();
  }

  public boolean isDebugLogging() {
    return debugLogging;
  }

  public List<String> getLoggingEnabled() {
    return loggingEnabled;
  }
}
