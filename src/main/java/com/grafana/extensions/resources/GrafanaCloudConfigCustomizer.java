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

  private static final Map<String, String> OVERRIDES = getOtlpCloudConfigs();

  static Map<String, String> getCustomProperties(ConfigProperties configs) {
    if (!OVERRIDES.isEmpty()) {
      // will need to mask header
      logger.info(
          "will attempt to send data to GrafanaCloud: configurations are set to: " + OVERRIDES);
    }
    return OVERRIDES;
  }

  static Map<String, String> getOtlpCloudConfigs() {
    Map<String, String> m = new HashMap<>();
    GrafanaCloudConfig configs = GrafanaCloudConfig.get();
    String exMessage = "will not attempt to send data to Grafana Cloud: %s is not set";
    try {
      Verify.verify(!configs.getApiKey().isEmpty(), exMessage, "apiKey");
      Verify.verify(!configs.getInstanceId().isEmpty(), exMessage, "instanceId");
      Verify.verify(!configs.getZone().isEmpty(), exMessage, "zone");
    } catch (VerifyException ve) {
      logger.info(ve.getMessage());
      return m;
    }
    m.put("otel.exporter.otlp.endpoint", getEndpoint(configs.getZone()));
    m.put("otel.exporter.otlp.protocol", configs.getProtocol());
    m.put(
        "otel.exporter.otlp.headers", getOtlpHeaders(configs.getInstanceId(), configs.getApiKey()));
    return m;
  }

  static String getOtlpHeaders(String instanceId, String apiKey) {
    String userPass = String.format("%s:%s", instanceId, apiKey);
    return String.format(
        "Authorization=Basic %s", Base64.getEncoder().encodeToString(userPass.getBytes()));
  }

  static String getEndpoint(String zone) {
    return StringUtils.isNotBlank(zone)
        ? String.format("https://otlp-gateway-%s.grafana.net/otlp", zone)
        : "";
  }
}
