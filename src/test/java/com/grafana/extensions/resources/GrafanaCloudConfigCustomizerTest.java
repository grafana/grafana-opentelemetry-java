/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaCloudConfig.*;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GrafanaCloudConfigCustomizerTest {

  @BeforeEach
  void clearSystemProperties() {
    System.clearProperty(CLOUD_API_KEY_PROP);
    System.clearProperty(CLOUD_INSTANCE_ID_PROP);
    System.clearProperty(CLOUD_ZONE_PROP);
    System.clearProperty("otel.exporter.otlp.endpoint");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCloudConfigurations")
  void getCustomProperties(
      String name, int instanceId, String apiKey, String zone, String endpoint, int expectedSize) {

    ConfigProperties configs = mock(ConfigProperties.class);
    when(configs.getString("otel.exporter.otlp.endpoint", "")).thenReturn(endpoint);
    when(configs.getString(CLOUD_ZONE_PROP, "")).thenReturn(zone);
    when(configs.getInt(CLOUD_INSTANCE_ID_PROP, 0)).thenReturn(instanceId);
    when(configs.getString(CLOUD_API_KEY_PROP, "")).thenReturn(apiKey);

    Map<String, String> m = GrafanaCloudConfigCustomizer.getCustomProperties(configs);
    // Map<String, String> m = GrafanaCloudConfigCustomizer.getOtlpCloudConfigs(configs);
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
        Arguments.of("only instanceId prop set", 12345, "", "", "", 0),
        Arguments.of("only apiKey prop set", 0, "fakeApiKey=", "", "", 0),
        Arguments.of("only zone prop set", 0, "", "fake-zone", "", 0),
        Arguments.of("only instanceId not set", 0, "fakeApiKey=", "fake-zone", "", 0),
        Arguments.of("only zone not set", 12345, "fakeApiKey=", "", "", 0),
        Arguments.of("only apiKey not set", 12345, "", "fake-zone", "", 0),
        Arguments.of("all cloud props set", 12345, "fakeApiKey=", "fake-zone", "", 3),
        Arguments.of(
            "otel endpoint takes precedence over cloud when all are set",
            12345,
            "fakeApiKey=",
            "fake-zone",
            "http://localhost:4317",
            0));
  }
}
