/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLanguageSpecificInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.InstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.util.Map;

public class DeclarativeConfigPropertyCustomizer {
  public static void addProperties(
      OpenTelemetryConfigurationModel model, Map<String, String> properties) {
    ExperimentalLanguageSpecificInstrumentationModel java = getJava(model);

    properties.forEach(
        (key, value) -> {
          if (!key.startsWith("otel.instrumentation.")) {
            throw new IllegalArgumentException("Invalid key: " + key);
          }
          String[] path =
              key.substring("otel.instrumentation.".length()).replace('-', '_').split("\\.");
          addProperty(java, path, value);
        });
  }

  private static ExperimentalLanguageSpecificInstrumentationModel getJava(
      OpenTelemetryConfigurationModel model) {
    InstrumentationModel instrumentationDevelopment = model.getInstrumentationDevelopment();
    if (instrumentationDevelopment == null) {
      instrumentationDevelopment = new InstrumentationModel();
      model.withInstrumentationDevelopment(instrumentationDevelopment);
    }
    ExperimentalLanguageSpecificInstrumentationModel java = instrumentationDevelopment.getJava();
    if (java == null) {
      java = new ExperimentalLanguageSpecificInstrumentationModel();
      instrumentationDevelopment.withJava(java);
    }
    return java;
  }

  @SuppressWarnings("unchecked")
  private static void addProperty(
      ExperimentalLanguageSpecificInstrumentationModel java, String[] path, String value) {
    // iterate through the suffix and create nested maps as needed
    Map<String, Object> current = java.getAdditionalProperties();

    for (int i = 0; i < path.length; i++) {
      String segment = path[i];
      if (segment.isEmpty()) {
        throw new IllegalArgumentException("Invalid key segment: empty segment at position " + i);
      }

      if (i == path.length - 1) {
        // last segment, set the value
        current.put(segment, value);
      } else {
        // intermediate segment, ensure it's a map
        Object next = current.get(segment);
        if (next == null) {
          // create a new map if it doesn't exist
          next = new java.util.HashMap<String, Object>();
          current.put(segment, next);
        } else if (!(next instanceof Map)) {
          throw new IllegalArgumentException("Invalid key segment: " + segment + " is not a map");
        }
        current = (Map<String, Object>) next;
      }
    }
  }
}
