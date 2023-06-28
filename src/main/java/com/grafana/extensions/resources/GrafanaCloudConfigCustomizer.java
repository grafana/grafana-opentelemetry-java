/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import com.google.common.base.Verify;
import com.google.common.base.VerifyException;
import com.grafana.extensions.resources.config.GrafanaConfig.GrafanaCloudConfig;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public final class GrafanaCloudConfigCustomizer {

  private static final Logger logger =
      Logger.getLogger(GrafanaCloudConfigCustomizer.class.getName());

  static Map<String, String> getCustomProperties(ConfigProperties configs) {
    String otelEndpoint = configs.getString("otel.exporter.otlp.endpoint", "");
    if (StringUtils.isNotBlank(otelEndpoint)) {
      logger.info("will attempt to send data to otel.exporter.otlp.endpoint: " + otelEndpoint);
      return new HashMap<>();
    }

    GrafanaCloudConfig cloudConfigs = new GrafanaCloudConfig(configs);
    Map<String, String> overrides = getOtlpCloudConfigs(cloudConfigs);
    return overrides;
  }

  static Map<String, String> getOtlpCloudConfigs(GrafanaCloudConfig configs) {
    Map<String, String> m = new HashMap<>();
    String exMessage = "will not attempt to send data to Grafana Cloud: %s is not set";

    String apiKey = configs.getApiKey();
    int instanceId = configs.getInstanceId();
    String zone = configs.getZone();

    try {
      Verify.verify(StringUtils.isNotBlank(zone), exMessage, "zone");
      Verify.verify(StringUtils.isNotBlank(apiKey), exMessage, "apiKey");
      Verify.verify(instanceId != 0, exMessage, "instanceId");
    } catch (VerifyException ve) {
      logger.info(ve.getMessage());
      return m;
    }
    String endpoint = getEndpoint(zone);
    m.put("otel.exporter.otlp.endpoint", endpoint);
    m.put("otel.exporter.otlp.headers", getOtlpHeaders(instanceId, apiKey));
    m.put("otel.exporter.otlp.protocol", configs.getProtocol());

    logger.info(
        String.format(
            "will attempt to send data to GrafanaCloud for endpoint {} and instance id {}",
            endpoint,
            instanceId));

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
