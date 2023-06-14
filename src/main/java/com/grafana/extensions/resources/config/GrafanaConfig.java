/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources.config;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public class GrafanaConfig {

  public static class GrafanaLoggingConfig {

    public static final String LOGGING_ENABLED_ENV = "GRAFANA_OTLP_LOGGING_ENABLED";
    public static final String DEBUG_LOGGING_ENV = "GRAFANA_OTLP_DEBUG_LOGGING";

    private boolean debugLogging;
    private String[] loggingEnabled;

    public static GrafanaLoggingConfig get() {
      return createConfigs();
    }

    public boolean isDebugLogging() {
      return debugLogging;
    }

    public String[] getLoggingEnabled() {
      return loggingEnabled;
    }

    private static GrafanaLoggingConfig createConfigs() {
      GrafanaLoggingConfig config = new GrafanaLoggingConfig();
      String value = getValue(DEBUG_LOGGING_ENV, "");
      config.debugLogging =
          StringUtils.isNotBlank(value) ? Boolean.parseBoolean(value) : Boolean.FALSE;

      value = getValue(LOGGING_ENABLED_ENV, "");
      config.loggingEnabled = StringUtils.isNotBlank(value) ? value.split(",") : null;
      return config;
    }
  }

  public static class GrafanaCloudConfig {
    public static final String CLOUD_API_KEY_ENV = "GRAFANA_OTLP_CLOUD_API_KEY";
    public static final String CLOUD_INSTANCE_ID_ENV = "GRAFANA_OTLP_CLOUD_INSTANCE_ID";
    public static final String CLOUD_ZONE_ENV = "GRAFANA_OTLP_CLOUD_ZONE";

    private GrafanaCloudConfig() {}

    public static GrafanaCloudConfig get() {
      return createConfigs();
    }

    private String instanceId;
    private String apiKey;
    private String zone;
    private final String protocol = "http/protobuf";

    public String getInstanceId() {
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

    private static GrafanaCloudConfig createConfigs() {
      GrafanaCloudConfig config = new GrafanaCloudConfig();
      config.apiKey = getValue(CLOUD_API_KEY_ENV, "");
      config.instanceId = getValue(CLOUD_INSTANCE_ID_ENV, "");
      config.zone = getValue(CLOUD_ZONE_ENV, "");
      return config;
    }
  }

  public static String getValue(String configKey, String defaultValue) {
    String value = System.getProperty(normalizeEnvironmentVariableKey(configKey));
    value = StringUtils.isNotBlank(value) ? value : System.getenv(configKey);
    return StringUtils.isNotBlank(value) ? value : defaultValue;
  }

  /**
   * Normalize an environment variable key by converting to lower case and replacing "_" with ".".
   */
  public static String normalizeEnvironmentVariableKey(String key) {
    return key.toLowerCase(Locale.ROOT).replace("_", ".");
  }
}
