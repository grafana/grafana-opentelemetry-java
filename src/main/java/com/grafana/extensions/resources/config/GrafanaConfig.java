/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources.config;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import org.apache.commons.lang3.StringUtils;

public class GrafanaConfig {

  public static class GrafanaLoggingConfig {

    public static final String LOGGING_ENABLED_PROP = "grafana.otlp.logging.exporter.enabled";
    public static final String DEBUG_LOGGING_PROP = "grafana.otlp.debug.logging";

    private boolean debugLogging;
    private String[] loggingEnabled;

    public GrafanaLoggingConfig(ConfigProperties configs) {
      this.debugLogging = configs.getBoolean(DEBUG_LOGGING_PROP, false);
      String loggingEnabled = configs.getString(LOGGING_ENABLED_PROP, "");
      this.loggingEnabled =
          StringUtils.isNotBlank(loggingEnabled) ? loggingEnabled.split(",") : null;
    }

    public boolean isDebugLogging() {
      return debugLogging;
    }

    public String[] getLoggingEnabled() {
      return loggingEnabled;
    }
  }

  public static class GrafanaCloudConfig {
    public static final String CLOUD_API_KEY_PROP = "grafana.otlp.cloud.api.key";
    public static final String CLOUD_INSTANCE_ID_PROP = "grafana.otlp.cloud.instance.id";
    public static final String CLOUD_ZONE_PROP = "grafana.otlp.cloud.zone";

    public GrafanaCloudConfig(ConfigProperties configs) {
      this.apiKey = configs.getString(CLOUD_API_KEY_PROP, "");
      this.instanceId = configs.getInt(CLOUD_INSTANCE_ID_PROP, 0);
      this.zone = configs.getString(CLOUD_ZONE_PROP, "");
    }

    private int instanceId;
    private String apiKey;
    private String zone;
    private final String protocol = "http/protobuf";

    public int getInstanceId() {
      return instanceId;
    }

    public String getApiKey() {
      return apiKey;
    }

    public String getZone() {
      return zone;
    }

    public String getProtocol() {
      return protocol;
    }

    @Override
    public String toString() {
      return "GrafanaCloudConfig{"
          + "instanceId='"
          + instanceId
          + '\''
          + ", apiKey='"
          + (apiKey.length() > 0 ? ((apiKey + "*****").substring(0, 5)) : "")
          + '\''
          + ", zone='"
          + zone
          + '\''
          + ", protocol='"
          + protocol
          + '\''
          + '}';
    }
  }
}
