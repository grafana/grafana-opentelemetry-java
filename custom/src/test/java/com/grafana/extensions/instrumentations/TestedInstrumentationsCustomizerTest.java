/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.instrumentations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

import com.grafana.extensions.VersionLogger;
import com.grafana.extensions.resources.internal.DistributionVersion;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestedInstrumentationsCustomizerTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create()
          .captureForType(VersionLogger.class)
          .captureForType(TestedInstrumentationsCustomizer.class);

  record TestCase(
      Map<String, String> inputProperties,
      Map<String, String> wantProperties,
      String wantLogStatement,
      String wantWarnings) {}

  @ParameterizedTest
  @MethodSource("testCases")
  void getCustomProperties(TestCase testCase) {
    DefaultConfigProperties configProperties =
        DefaultConfigProperties.createFromMap(testCase.inputProperties);
    configProperties =
        configProperties.withOverrides(
            TestedInstrumentationsCustomizer.customizeProperties(configProperties));

    assertThat(TestedInstrumentationsCustomizer.getAllProperties(configProperties))
        .containsAllEntriesOf(testCase.wantProperties);
    assertThat(logs.getEvents()).hasSize(testCase.wantWarnings.isEmpty() ? 1 : 2);
    logs.assertContains(testCase.wantWarnings);
    logs.assertContains(testCase.wantLogStatement);
  }

  private static Stream<Arguments> testCases() {
    String testedVersion =
        String.format(
            "Grafana OpenTelemetry Javaagent: version=%s, includeAllInstrumentations=false,"
                + " useTestedInstrumentations=true, includedUntestedInstrumentations=[],"
                + " excludedInstrumentations=[]",
            DistributionVersion.VERSION);
    return Stream.of(
        Arguments.of(
            named(
                "all instrumentations are included by default",
                new TestCase(
                    Map.of(),
                    Map.of(),
                    ("Grafana OpenTelemetry Javaagent: version=%s, includeAllInstrumentations=true,"
                            + " useTestedInstrumentations=false, includedUntestedInstrumentations=[],"
                            + " excludedInstrumentations=[]")
                        .formatted(DistributionVersion.VERSION),
                    ""))),
        Arguments.of(
            named(
                "use tested instrumentations - exactly those",
                new TestCase(
                    Map.of("grafana.otel.use-tested-instrumentations", "true"),
                    Map.of(
                        "otel.instrumentation.common.default.enabled", "false",
                        "otel.instrumentation.spring.data.enabled", "true",
                        "otel.instrumentation.jms.enabled", "true"),
                    testedVersion,
                    ""))),
        Arguments.of(
            named(
                "exclude an instrumentation is not allowed",
                new TestCase(
                    Map.of(
                        "grafana.otel.use-tested-instrumentations",
                        "true",
                        "otel.instrumentation.spring.data.enabled",
                        "false"),
                    Map.of(
                        "otel.instrumentation.common.default.enabled", "false",
                        "otel.instrumentation.spring.data.enabled", "true",
                        "otel.instrumentation.jms.enabled", "true"),
                    testedVersion,
                    "Including instrumentation spring.data again (remove"
                        + " grafana.otel.use-tested-instrumentations=true to remove this"
                        + " restriction)"))),
        Arguments.of(
            named(
                "instrumentation play included - excluded again, because "
                    + "grafana.otel.use-tested-instrumentations was found",
                new TestCase(
                    Map.of(
                        "otel.instrumentation.play.enabled",
                        "true",
                        "grafana.otel.use-tested-instrumentations",
                        "true"),
                    Map.of(
                        "otel.instrumentation.common.default.enabled",
                        "false",
                        "otel.instrumentation.spring.data.enabled",
                        "true",
                        "otel.instrumentation.jms.enabled",
                        "true",
                        "otel.instrumentation.play.enabled",
                        "false"),
                    testedVersion,
                    "Excluding untested instrumentation play (remove"
                        + " grafana.otel.use-tested-instrumentations=true to remove this"
                        + " restriction)"))),
        Arguments.of(
            named(
                "set all instrumentations included by default - excluded again, because "
                    + "grafana.otel.use-tested-instrumentations was found",
                new TestCase(
                    Map.of(
                        "otel.instrumentation.common.default.enabled",
                        "true",
                        "grafana.otel.use-tested-instrumentations",
                        "true"),
                    Map.of(
                        "otel.instrumentation.common.default.enabled",
                        "false",
                        "otel.instrumentation.spring.data.enabled",
                        "true",
                        "otel.instrumentation.jms.enabled",
                        "true"),
                    testedVersion,
                    "Excluding otel.instrumentation.common.default.enabled (remove"
                        + " grafana.otel.use-tested-instrumentations=true to remove this"
                        + " restriction)"))),
        Arguments.of(
            named(
                "instrumentation play included - but not all enabled by default",
                new TestCase(
                    Map.of(
                        "otel.instrumentation.play.enabled",
                        "true",
                        "otel.instrumentation.common.default.enabled",
                        "false"),
                    Map.of(
                        "otel.instrumentation.play.enabled",
                        "true",
                        "otel.instrumentation.common.default.enabled",
                        "false"),
                    ("Grafana OpenTelemetry Javaagent: version=%s,"
                            + " includeAllInstrumentations=false, useTestedInstrumentations=false,"
                            + " includedUntestedInstrumentations=[play], excludedInstrumentations=[]")
                        .formatted(DistributionVersion.VERSION),
                    ""))));
  }
}
