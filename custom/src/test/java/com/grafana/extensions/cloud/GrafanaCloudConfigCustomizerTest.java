/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.cloud;

import static com.grafana.extensions.cloud.GrafanaCloudConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GrafanaCloudConfigCustomizerTest {

  record TestCase(
      int instanceId,
      String apiKey,
      String zone,
      String endpoint,
      Map<String, String> expectedOutput) {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideCloudConfigurations")
  void getCustomProperties(String name, TestCase testCase) {

    Map<String, String> props =
        Map.of(
            "otel.exporter.otlp.endpoint",
            testCase.endpoint,
            CLOUD_ZONE_PROP,
            testCase.zone,
            CLOUD_INSTANCE_ID_PROP,
            String.valueOf(testCase.instanceId),
            CLOUD_API_KEY_PROP,
            testCase.apiKey);
    DefaultConfigProperties defaultConfigs = DefaultConfigProperties.createFromMap(props);
    Map<String, String> m = GrafanaCloudConfigCustomizer.customizeProperties(defaultConfigs);
    assertThat(m).isEqualTo(testCase.expectedOutput);
  }

  private static Stream<Arguments> provideCloudConfigurations() {
    Map<String, String> emptyMap = new HashMap<>();
    return Stream.of(
        Arguments.of("only instanceId prop set", new TestCase(12345, "", "", "", emptyMap)),
        Arguments.of("only apiKey prop set", new TestCase(0, "fakeApiKey=", "", "", emptyMap)),
        Arguments.of("only zone prop set", new TestCase(0, "", "fake-zone", "", emptyMap)),
        Arguments.of(
            "only instanceId not set", new TestCase(0, "fakeApiKey=", "fake-zone", "", emptyMap)),
        Arguments.of("only zone not set", new TestCase(12345, "fakeApiKey=", "", "", emptyMap)),
        Arguments.of("only apiKey not set", new TestCase(12345, "", "fake-zone", "", emptyMap)),
        Arguments.of(
            "all cloud props set",
            new TestCase(
                12345,
                "fakeApiKey=",
                "fake-zone",
                "",
                Map.of(
                    "otel.exporter.otlp.protocol",
                    "http/protobuf",
                    "otel.exporter.otlp.endpoint",
                    String.format("https://otlp-gateway-%s.grafana.net/otlp", "fake-zone"),
                    "otel.exporter.otlp.headers",
                    GrafanaCloudConfigCustomizer.getOtlpHeaders(12345, "fakeApiKey=")))),
        Arguments.of(
            "otel endpoint takes precedence over cloud when all are set",
            new TestCase(12345, "fakeApiKey=", "fake-zone", "http://localhost:4317", emptyMap)));
  }
}
