/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("DataFlowIssue")
class DeclarativeConfigPropertyCustomizerTest {
  @Test
  void testAddSimpleProperty() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    DeclarativeConfigPropertyCustomizer.addProperties(
        model, Map.of("otel.instrumentation.foo", "baz"));
    assertThat(model.getInstrumentationDevelopment().getJava().getAdditionalProperties())
        .extracting("foo")
        .isEqualTo("baz");
  }

  @Test
  void testAddNestedProperty() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    DeclarativeConfigPropertyCustomizer.addProperties(
        model, Map.of("otel.instrumentation.foo.bar", "baz"));
    assertThat(model.getInstrumentationDevelopment().getJava().getAdditionalProperties())
        .extracting("foo")
        .extracting("bar")
        .isEqualTo("baz");
  }

  @Test
  void testInvalidKeyThrows() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    Map<String, String> properties = new HashMap<>();
    properties.put("invalid.key", "value");
    assertThatCode(() -> DeclarativeConfigPropertyCustomizer.addProperties(model, properties))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testEmptySegmentThrows() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    Map<String, String> properties = new HashMap<>();
    properties.put("otel.instrumentation..foo", "value");
    assertThatCode(() -> DeclarativeConfigPropertyCustomizer.addProperties(model, properties))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testNonMapIntermediateThrows() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    // First, set foo to a value
    DeclarativeConfigPropertyCustomizer.addProperties(
        model, Map.of("otel.instrumentation.foo", "value1"));
    // Now, try to set foo.bar, which should fail
    assertThatCode(
            () ->
                DeclarativeConfigPropertyCustomizer.addProperties(
                    model, Map.of("otel.instrumentation.foo.bar", "value2")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
