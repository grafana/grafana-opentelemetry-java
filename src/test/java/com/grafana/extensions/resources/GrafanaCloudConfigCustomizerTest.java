/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaCloudConfig.CLOUD_API_KEY_ENV;
import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaCloudConfig.CLOUD_INSTANCE_ID_ENV;
import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaCloudConfig.CLOUD_ZONE_ENV;
import static com.grafana.extensions.resources.config.GrafanaConfig.normalizeEnvironmentVariableKey;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GrafanaCloudConfigCustomizerTest {

  private static final String CLOUD_INSTANCE_ID_PROP =
      normalizeEnvironmentVariableKey(CLOUD_INSTANCE_ID_ENV);
  private static final String CLOUD_API_KEY_PROP =
      normalizeEnvironmentVariableKey(CLOUD_API_KEY_ENV);
  private static final String CLOUD_ZONE_PROP = normalizeEnvironmentVariableKey(CLOUD_ZONE_ENV);

  @BeforeEach
  void clearSystemProperties() {
    System.clearProperty(CLOUD_API_KEY_PROP);
    System.clearProperty(CLOUD_INSTANCE_ID_PROP);
    System.clearProperty(CLOUD_ZONE_PROP);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCloudConfigurations")
  void getCustomProperties(
      String name, String instanceId, String apiKey, String zone, int expectedSize) {
    if (StringUtils.isNotBlank(instanceId)) {
      System.setProperty(CLOUD_INSTANCE_ID_PROP, instanceId);
    }
    if (StringUtils.isNotBlank(apiKey)) {
      System.setProperty(CLOUD_API_KEY_PROP, apiKey);
    }
    if (StringUtils.isNotBlank(zone)) {
      System.setProperty(CLOUD_ZONE_PROP, zone);
    }
    Map<String, String> m = GrafanaCloudConfigCustomizer.getOtlpCloudConfigs();
    assertThat(m.size()).isEqualTo(expectedSize);
    if (expectedSize > 3) {
      assertThat(m)
          .containsOnly(
              entry("otel.exporter.otlp.protocol", "http/protobuf"),
              entry(
                  "otel.exporter.otlp.endpoint",
                  String.format("https://otlp-gateway-%s.grafana.net/otlp", zone)),
              entry(
                  "otel.exporter.otlp.headers",
                  GrafanaCloudConfigCustomizer.getOtlpHeaders(instanceId, apiKey)));
    }
  }

  private static Stream<Arguments> provideCloudConfigurations() {
    return Stream.of(
        Arguments.of("only instanceId prop set", "12345", "", "", 0),
        Arguments.of("only apiKey prop set", "", "fakeApiKey=", "", 0),
        Arguments.of("only apiKey prop set", "", "", "fake-zone", 0),
        Arguments.of("only instanceId not set", "", "fakeApiKey=", "fake-zone", 0),
        Arguments.of("only zone not set", "12345", "fakeApiKey=", "", 0),
        Arguments.of("only apiKey not set", "12345", "", "fake-zone", 0),
        Arguments.of("all cloud props set", "12345", "fakeApiKey=", "fake-zone", 3));
  }
}
