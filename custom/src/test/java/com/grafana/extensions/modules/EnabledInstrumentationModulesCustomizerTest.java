/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.grafana.extensions.resources.internal.DistributionVersion;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EnabledInstrumentationModulesCustomizerTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create()
          .captureForType(EnabledInstrumentationModulesCustomizer.class)
          .captureForType(SupportContext.class);

  static class TestCase {
    Map<String, String> inputProperties;
    Map<String, String> wantProperties;
    String wantWarnings;
    String wantSupportStatement;

    public TestCase(
        Map<String, String> inputProperties,
        Map<String, String> wantProperties,
        String wantSupportStatement,
        String expectedOutput) {
      this.inputProperties = inputProperties;
      this.wantProperties = wantProperties;
      this.wantSupportStatement = wantSupportStatement;
      this.wantWarnings = expectedOutput;
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
    assertThat(logs.getEvents()).hasSize(testCase.wantWarnings.isEmpty() ? 1 : 2);
    logs.assertContains(testCase.wantWarnings);
    logs.assertContains(testCase.wantSupportStatement);
  }

  private static Stream<Arguments> testCases() {
    String supportedVersion =
        String.format(
            "Grafana OpenTelemetry Javaagent: version=%s, "
                + "enableAllInstrumentations=false, enabledUnsupportedInstrumentations=[], disabledInstrumentations=[]",
            DistributionVersion.VERSION);
    return Stream.of(
        Arguments.of(
            "no input - only supported are enabled",
            new TestCase(
                Collections.emptyMap(),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled", "false",
                    "otel.instrumentation.spring.data.enabled", "true",
                    "otel.instrumentation.jms.enabled", "true"),
                supportedVersion,
                "")),
        Arguments.of(
            "enable unsupported modules without any other option doesn't cause a warning",
            new TestCase(
                ImmutableMap.of("grafana.otel.instrumentation.enable-unsupported-modules", "true"),
                ImmutableMap.of(
                    "grafana.otel.instrumentation.enable.unsupported.modules",
                    "true",
                    "otel.instrumentation.common.default.enabled",
                    "false",
                    "otel.instrumentation.spring.data.enabled",
                    "true",
                    "otel.instrumentation.jms.enabled",
                    "true"),
                supportedVersion,
                "")),
        Arguments.of(
            "disable a module is not allowed",
            new TestCase(
                ImmutableMap.of("otel.instrumentation.spring.data.enabled", "false"),
                ImmutableMap.of(
                    "otel.instrumentation.common.default.enabled", "false",
                    "otel.instrumentation.spring.data.enabled", "true",
                    "otel.instrumentation.jms.enabled", "true"),
                supportedVersion,
                "Enabling module spring.data again (set "
                    + "grafana.otel.instrumentation.enable-unsupported-modules=true to remove this restriction)")),
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
                supportedVersion,
                "Disabling unsupported module foo (set "
                    + "grafana.otel.instrumentation.enable-unsupported-modules=true to remove this restriction)")),
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
                supportedVersion,
                "Disabling otel.instrumentation.common.default.enabled (set "
                    + "grafana.otel.instrumentation.enable-unsupported-modules=true to remove this restriction)")),
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
                    "grafana.otel.instrumentation.enable.unsupported.modules",
                    "true",
                    "otel.instrumentation.common.default.enabled",
                    "false",
                    "otel.instrumentation.spring.data.enabled",
                    "true",
                    "otel.instrumentation.jms.enabled",
                    "true",
                    "otel.instrumentation.foo.enabled",
                    "true"),
                "Grafana OpenTelemetry Javaagent is UNSUPPORTED: version=0.1, enableAllInstrumentations=false, "
                    + "enabledUnsupportedInstrumentations=[foo], disabledInstrumentations=[] (The javaagent is running in "
                    + "unsupported mode, please remove the -Dgrafana.otel.instrumentation.enable-unsupported-modules=true "
                    + "command line argument or GRAFANA_OTEL_INSTRUMENTATION_ENABLE_UNSUPPORTED_MODULES=true environment "
                    + "variable to turn on the supported mode)",
                "")),
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
                    "grafana.otel.instrumentation.enable.unsupported.modules",
                    "true",
                    "otel.instrumentation.common.default.enabled",
                    "true",
                    "otel.instrumentation.spring.data.enabled",
                    "true",
                    "otel.instrumentation.jms.enabled",
                    "true"),
                "Grafana OpenTelemetry Javaagent is UNSUPPORTED: version=0.1, enableAllInstrumentations=true, "
                    + "enabledUnsupportedInstrumentations=[], disabledInstrumentations=[] (The javaagent is running in "
                    + "unsupported mode, please remove the -Dgrafana.otel.instrumentation.enable-unsupported-modules=true "
                    + "command line argument or GRAFANA_OTEL_INSTRUMENTATION_ENABLE_UNSUPPORTED_MODULES=true environment "
                    + "variable to turn on the supported mode)",
                "")));
  }
}
