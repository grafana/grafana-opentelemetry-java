/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

class EnabledInstrumentationModulesCustomizerTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForType(EnabledInstrumentationModulesCustomizer.class);

  static class TestCase {
    Map<String, String> inputProperties;
    Map<String, String> wantProperties;
    String expectedOutput;

    public TestCase(
        Map<String, String> inputProperties,
        Map<String, String> wantProperties,
        String expectedOutput) {
      this.inputProperties = inputProperties;
      this.wantProperties = wantProperties;
      this.expectedOutput = expectedOutput;
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void getCustomProperties(String name, TestCase testCase) {

    DefaultConfigProperties configProperties =
        DefaultConfigProperties.createForTest(
                EnabledInstrumentationModulesCustomizer.getDefaultProperties())
            .withOverrides(testCase.inputProperties);

    configProperties =
        configProperties.withOverrides(
            EnabledInstrumentationModulesCustomizer.customizeProperties(configProperties));

    assertThat(EnabledInstrumentationModulesCustomizer.getAllProperties(configProperties))
        .containsAllEntriesOf(testCase.wantProperties);
    if (testCase.expectedOutput.isEmpty()) {
      assertThat(logs.getEvents()).isEmpty();
    } else {
      logs.assertContains(testCase.expectedOutput);
    }
  }

  private static Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of(
            "no input - only supported are enabled",
            new TestCase(
                Collections.emptyMap(),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled", "false",
                    "otel.instrumentation.spring.data.enabled", "true",
                    "otel.instrumentation.jms.enabled", "true"),
                "")),
        Arguments.of(
            "disable a module is allowed",
            new TestCase(
                ImmutableMap.of("otel.instrumentation.spring.data.enabled", "false"),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled", "false",
                    "otel.instrumentation.spring.data.enabled", "false",
                    "otel.instrumentation.jms.enabled", "true"),
                "")),
        Arguments.of(
            "module foo enabled - disabled again, because "
                + "grafana.otel.instrumentation.enable-unsupported-modules was not found",
            new TestCase(
                ImmutableMap.of("otel.instrumentation.foo.enabled", "true"),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled",
                    "false",
                    "otel.instrumentation.spring.data.enabled",
                    "true",
                    "otel.instrumentation.jms.enabled",
                    "true",
                    "otel.instrumentation.foo.enabled",
                    "false"),
                "Disabling unsupported module foo (set "
                    + "grafana.otel.instrumentation.enable-unsupported-modules=true to enable unsupported modules)")),
        Arguments.of(
            "set all modules enabled by default - disabled again, because "
                + "grafana.otel.instrumentation.enable-unsupported-modules was not found",
            new TestCase(
                ImmutableMap.of("otel.instrumentation.common.default.enabled", "true"),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled",
                    "false",
                    "otel.instrumentation.spring.data.enabled",
                    "true",
                    "otel.instrumentation.jms.enabled",
                    "true"),
                "Disabling otel.instrumentation.common.default.enabled (set "
                    + "grafana.otel.instrumentation.enable-unsupported-modules=true to be able to enable all modules)")),
        Arguments.of(
            "module foo enabled - allowed, because "
                + "grafana.otel.instrumentation.enable-unsupported-modules was not found",
            new TestCase(
                ImmutableMap.of(
                    "grafana.otel.instrumentation.enable-unsupported-modules",
                    "true",
                    "otel.instrumentation.foo.enabled",
                    "true"),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled",
                    "false",
                    "otel.instrumentation.spring.data.enabled",
                    "true",
                    "otel.instrumentation.jms.enabled",
                    "true",
                    "otel.instrumentation.foo.enabled",
                    "true"),
                "Enabling unsupported modules")),
        Arguments.of(
            "set all modules enabled by default - allowed, because "
                + "grafana.otel.instrumentation.enable-unsupported-modules was not found",
            new TestCase(
                ImmutableMap.of(
                    "grafana.otel.instrumentation.enable-unsupported-modules",
                    "true",
                    "otel.instrumentation.common.default.enabled",
                    "true"),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled",
                    "true",
                    "otel.instrumentation.spring.data.enabled",
                    "true",
                    "otel.instrumentation.jms.enabled",
                    "true"),
                "Enabling unsupported modules")));
  }
}
