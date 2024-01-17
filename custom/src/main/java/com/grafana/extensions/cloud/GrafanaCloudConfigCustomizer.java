/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.cloud;

import com.grafana.extensions.util.StringUtils;
import com.grafana.extensions.util.Verify;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Deprecated
public final class GrafanaCloudConfigCustomizer {

  private static final Logger logger =
      Logger.getLogger(GrafanaCloudConfigCustomizer.class.getName());

  public static Map<String, String> customizeProperties(ConfigProperties configs) {
    String otelEndpoint = configs.getString("otel.exporter.otlp.endpoint", "");
    if (StringUtils.isNotBlank(otelEndpoint)) {
      logger.info("will attempt to send data to otel.exporter.otlp.endpoint: " + otelEndpoint);
      return new HashMap<>();
    }

    GrafanaCloudConfig cloudConfigs =
        new GrafanaCloudConfig(
            configs.getString(GrafanaCloudConfig.CLOUD_API_KEY_PROP, ""),
            configs.getInt(GrafanaCloudConfig.CLOUD_INSTANCE_ID_PROP, 0),
            configs.getString(GrafanaCloudConfig.CLOUD_ZONE_PROP, ""));
    return getOtlpCloudConfigs(cloudConfigs);
  }

  static Map<String, String> getOtlpCloudConfigs(GrafanaCloudConfig configs) {
    Map<String, String> m = new HashMap<>();
    String exMessage = "will not attempt to send data to Grafana Cloud: %s is not set";

    String apiKey = configs.getApiKey();
    int instanceId = configs.getInstanceId();
    String zone = configs.getZone();
    String protocol = configs.getProtocol();

    try {
      Verify.verify(StringUtils.isNotBlank(zone), exMessage, "zone");
      Verify.verify(StringUtils.isNotBlank(apiKey), exMessage, "apiKey");
      Verify.verify(instanceId != 0, exMessage, "instanceId");
    } catch (RuntimeException ve) {
      logger.info(ve.getMessage());
      return m;
    }
    String endpoint = getEndpoint(zone);
    m.put("otel.exporter.otlp.endpoint", endpoint);
    m.put("otel.exporter.otlp.headers", getOtlpHeaders(instanceId, apiKey));
    m.put("otel.exporter.otlp.protocol", protocol);

    logger.info(
        String.format(
            "will attempt to send data to GrafanaCloud for endpoint %s, protocol %s",
            endpoint, protocol));

    return m;
  }

  static String getOtlpHeaders(int instanceId, String apiKey) {
    String userPass = String.format("%d:%s", instanceId, apiKey);
    return String.format(
        "Authorization=Basic %s", Base64.getEncoder().encodeToString(userPass.getBytes()));
  }

  static String getEndpoint(String zone) {
    return StringUtils.isNotBlank(zone)
        ? String.format("https://otlp-gateway-%s.grafana.net/otlp", zone)
        : "";
  }
}
