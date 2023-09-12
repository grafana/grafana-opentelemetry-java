/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import com.grafana.extensions.resources.internal.DistributionVersion;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TestedModulesCustomizerTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create()
          .captureForType(TestedModulesCustomizer.class)
          .captureForType(TestedModulesContext.class);

  record TestCase(
      Map<String, String> inputProperties,
      Map<String, String> wantProperties,
      String wantLogStatement,
      String wantWarnings) {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void getCustomProperties(String name, TestCase testCase) {

    DefaultConfigProperties configProperties =
        DefaultConfigProperties.createForTest(
                TestedModulesCustomizer.getDefaultProperties())
            .withOverrides(testCase.inputProperties);

    configProperties =
        configProperties.withOverrides(
            TestedModulesCustomizer.customizeProperties(configProperties));

    assertThat(TestedModulesCustomizer.getAllProperties(configProperties))
        .containsAllEntriesOf(testCase.wantProperties);
    assertThat(logs.getEvents()).hasSize(testCase.wantWarnings.isEmpty() ? 1 : 2);
    logs.assertContains(testCase.wantWarnings);
    logs.assertContains(testCase.wantLogStatement);
  }

  private static Stream<Arguments> testCases() {
    String testedVersion =
        String.format(
            "Grafana OpenTelemetry Javaagent: version=%s, "
                + "enableAllInstrumentations=false, includedUntestedInstrumentations=[], excludedInstrumentations=[]",
            DistributionVersion.VERSION);
    return Stream.of(
        Arguments.of(
            "no input - only tested are included",
            new TestCase(
                Collections.emptyMap(),
                Map.of(
                    "otel.instrumentation.common.default.included", "false",
                    "otel.instrumentation.spring.data.included", "true",
                    "otel.instrumentation.jms.included", "true"),
                testedVersion,
                "")),
        Arguments.of(
            "enable untested modules without any other option doesn't cause a warning",
            new TestCase(
                Map.of("grafana.otel.instrumentation.exclude-untested-modules", "true"),
                Map.of(
                    "grafana.otel.instrumentation.enable.untested.modules",
                    "true",
                    "otel.instrumentation.common.default.included",
                    "false",
                    "otel.instrumentation.spring.data.included",
                    "true",
                    "otel.instrumentation.jms.included",
                    "true"),
                testedVersion,
                "")),
        Arguments.of(
            "exclude a module is not allowed",
            new TestCase(
                Map.of("otel.instrumentation.spring.data.included", "false"),
                Map.of(
                    "otel.instrumentation.common.default.included", "false",
                    "otel.instrumentation.spring.data.included", "true",
                    "otel.instrumentation.jms.included", "true"),
                testedVersion,
                "Excluding module spring.data again (set "
                    + "grafana.otel.instrumentation.exclude-untested-modules=true to remove this restriction)")),
        Arguments.of(
            "module foo included - excluded again, because "
                + "grafana.otel.instrumentation.exclude-untested-modules was not found",
            new TestCase(
                Map.of("otel.instrumentation.foo.included", "true"),
                Map.of(
                    "otel.instrumentation.common.default.included",
                    "false",
                    "otel.instrumentation.spring.data.included",
                    "true",
                    "otel.instrumentation.jms.included",
                    "true",
                    "otel.instrumentation.foo.included",
                    "false"),
                testedVersion,
                "Excluding untested module foo (set "
                    + "grafana.otel.instrumentation.exclude-untested-modules=true to remove this restriction)")),
        Arguments.of(
            "set all modules included by default - excluded again, because "
                + "grafana.otel.instrumentation.exclude-untested-modules was not found",
            new TestCase(
                Map.of("otel.instrumentation.common.default.included", "true"),
                Map.of(
                    "otel.instrumentation.common.default.included",
                    "false",
                    "otel.instrumentation.spring.data.included",
                    "true",
                    "otel.instrumentation.jms.included",
                    "true"),
                testedVersion,
                "Excluding otel.instrumentation.common.default.included (set "
                    + "grafana.otel.instrumentation.exclude-untested-modules=true to remove this restriction)")),
        Arguments.of(
            "module foo included - allowed, because "
                + "grafana.otel.instrumentation.exclude-untested-modules was not found",
            new TestCase(
                Map.of(
                    "grafana.otel.instrumentation.exclude-untested-modules",
                    "true",
                    "otel.instrumentation.foo.included",
                    "true"),
                Map.of(
                    "grafana.otel.instrumentation.enable.untested.modules",
                    "true",
                    "otel.instrumentation.common.default.included",
                    "false",
                    "otel.instrumentation.spring.data.included",
                    "true",
                    "otel.instrumentation.jms.included",
                    "true",
                    "otel.instrumentation.foo.included",
                    "true"),
                ("Grafana OpenTelemetry Javaagent is UNTESTED: version=%s, enableAllInstrumentations=false, "
                        + "includedUntestedInstrumentations=[foo], excludedInstrumentations=[] (The javaagent is "
                        + "running in untested mode, please remove the "
                        + "-Dgrafana.otel.instrumentation.exclude-untested-modules=true command line argument or "
                        + "GRAFANA_OTEL_INSTRUMENTATION_ENABLE_UNTESTED_MODULES=true environment "
                        + "variable to turn on the tested mode)")
                    .formatted(DistributionVersion.VERSION),
                "")),
        Arguments.of(
            "set all modules included by default - allowed, because "
                + "grafana.otel.instrumentation.exclude-untested-modules was not found",
            new TestCase(
                Map.of(
                    "grafana.otel.instrumentation.exclude-untested-modules",
                    "true",
                    "otel.instrumentation.common.default.included",
                    "true"),
                Map.of(
                    "grafana.otel.instrumentation.enable.untested.modules",
                    "true",
                    "otel.instrumentation.common.default.included",
                    "true",
                    "otel.instrumentation.spring.data.included",
                    "true",
                    "otel.instrumentation.jms.included",
                    "true"),
                ("Grafana OpenTelemetry Javaagent is UNTESTED: version=%s, enableAllInstrumentations=true, "
                        + "includedUntestedInstrumentations=[], excludedInstrumentations=[] (The javaagent is "
                        + "running in untested mode, please remove the "
                        + "-Dgrafana.otel.instrumentation.exclude-untested-modules=true command line argument or "
                        + "GRAFANA_OTEL_INSTRUMENTATION_ENABLE_UNTESTED_MODULES=true environment variable to turn "
                        + "on the tested mode)")
                    .formatted(DistributionVersion.VERSION),
                "")));
  }
}
