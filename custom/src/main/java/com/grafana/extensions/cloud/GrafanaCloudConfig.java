/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.cloud;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

public class GrafanaCloudConfig {
  public static final String CLOUD_API_KEY_PROP = "grafana.otlp.cloud.api.key";
  public static final String CLOUD_INSTANCE_ID_PROP = "grafana.otlp.cloud.instance.id";
  public static final String CLOUD_ZONE_PROP = "grafana.otlp.cloud.zone";

  public GrafanaCloudConfig(ConfigProperties configs) {
    this.apiKey = configs.getString(CLOUD_API_KEY_PROP, "");
    this.instanceId = configs.getInt(CLOUD_INSTANCE_ID_PROP, 0);
    this.zone = configs.getString(CLOUD_ZONE_PROP, "");
  }

  private final int instanceId;
  private final String apiKey;
  private final String zone;
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
