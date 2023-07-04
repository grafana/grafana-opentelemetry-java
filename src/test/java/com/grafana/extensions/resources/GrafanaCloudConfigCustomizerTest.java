/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static com.grafana.extensions.resources.config.GrafanaConfig.GrafanaCloudConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GrafanaCloudConfigCustomizerTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCloudConfigurations")
  void getCustomProperties(
      String name,
      int instanceId,
      String apiKey,
      String zone,
      String endpoint,
      Map<String, String> expectedOutput) {

    Map<String, String> props =
        ImmutableMap.of(
            "otel.exporter.otlp.endpoint",
            endpoint,
            CLOUD_ZONE_PROP,
            zone,
            CLOUD_INSTANCE_ID_PROP,
            String.valueOf(instanceId),
            CLOUD_API_KEY_PROP,
            apiKey);
    DefaultConfigProperties defaultConfigs = DefaultConfigProperties.createForTest(props);
    Map<String, String> m = GrafanaCloudConfigCustomizer.getCustomProperties(defaultConfigs);
    assertThat(m.size()).isEqualTo(expectedOutput.size());
    assertThat(m).isEqualTo(expectedOutput);
  }

  private static Stream<Arguments> provideCloudConfigurations() {
    Map<String, String> emptyMap = new HashMap<>();
    return Stream.of(
        Arguments.of("only instanceId prop set", 12345, "", "", "", emptyMap),
        Arguments.of("only apiKey prop set", 0, "fakeApiKey=", "", "", emptyMap),
        Arguments.of("only zone prop set", 0, "", "fake-zone", "", emptyMap),
        Arguments.of("only instanceId not set", 0, "fakeApiKey=", "fake-zone", "", emptyMap),
        Arguments.of("only zone not set", 12345, "fakeApiKey=", "", "", emptyMap),
        Arguments.of("only apiKey not set", 12345, "", "fake-zone", "", emptyMap),
        Arguments.of(
            "all cloud props set",
            12345,
            "fakeApiKey=",
            "fake-zone",
            "",
            ImmutableMap.of(
                "otel.exporter.otlp.protocol",
                "http/protobuf",
                "otel.exporter.otlp.endpoint",
                String.format("https://otlp-gateway-%s.grafana.net/otlp", "fake-zone"),
                "otel.exporter.otlp.headers",
                GrafanaCloudConfigCustomizer.getOtlpHeaders(12345, "fakeApiKey="))),
        Arguments.of(
            "otel endpoint takes precedence over cloud when all are set",
            12345,
            "fakeApiKey=",
            "fake-zone",
            "http://localhost:4317",
            emptyMap));
  }
}
