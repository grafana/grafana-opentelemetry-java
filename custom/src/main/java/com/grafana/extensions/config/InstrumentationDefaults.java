/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.config;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLanguageSpecificInstrumentationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalLanguageSpecificInstrumentationPropertyModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines instrumentation defaults using DC-style structured navigation.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * InstrumentationDefaults defaults = new InstrumentationDefaults();
 * defaults.getStructured("micrometer").setDefault("base_time_unit", "s");
 * defaults.getStructured("log4j_appender").setDefault("experimental_log_attributes", "true");
 *
 * // DC mode: inject into model
 * customizer.addModelCustomizer(model -> defaults.applyToModel(model));
 *
 * // Non-DC mode: translate to ConfigProperties
 * autoConfiguration.addPropertiesSupplier(defaults::toConfigProperties);
 * }</pre>
 *
 * <p>TODO: Propose as shared utility in otel-java-contrib for distros.
 */
public class InstrumentationDefaults {

  private final Map<String, InstrumentationPropertyDefaults> instrumentations =
      new LinkedHashMap<>();

  /** Navigate to a specific instrumentation, creating it if needed. */
  public InstrumentationPropertyDefaults getStructured(String instrumentation) {
    return instrumentations.computeIfAbsent(
        instrumentation, k -> new InstrumentationPropertyDefaults());
  }

  /** Translate defaults to {@code otel.instrumentation.*} keys for auto-configuration. */
  public Map<String, String> toConfigProperties() {
    HashMap<String, String> map = new HashMap<>();
    instrumentations.forEach(
        (instrumentation, properties) ->
            properties
                .getDefaults()
                .forEach(
                    (key, value) ->
                        map.put(
                            "otel.instrumentation."
                                + instrumentation.replace('_', '-')
                                + "."
                                + key.replace('_', '-'),
                            value)));
    return map;
  }

  /** Apply defaults to the DC model (under instrumentation/development.java). */
  public OpenTelemetryConfigurationModel applyToModel(OpenTelemetryConfigurationModel model) {
    if (instrumentations.isEmpty()) {
      return model;
    }

    ExperimentalInstrumentationModel instrumentation = model.getInstrumentationDevelopment();
    if (instrumentation == null) {
      instrumentation = new ExperimentalInstrumentationModel();
      model.withInstrumentationDevelopment(instrumentation);
    }
    ExperimentalLanguageSpecificInstrumentationModel java = instrumentation.getJava();
    if (java == null) {
      java = new ExperimentalLanguageSpecificInstrumentationModel();
      instrumentation.withJava(java);
    }

    Map<String, ExperimentalLanguageSpecificInstrumentationPropertyModel> props =
        java.getAdditionalProperties();

    for (Map.Entry<String, InstrumentationPropertyDefaults> entry : instrumentations.entrySet()) {
      String name = entry.getKey();
      Map<String, String> defaults = entry.getValue().getDefaults();

      ExperimentalLanguageSpecificInstrumentationPropertyModel propModel = props.get(name);
      if (propModel == null) {
        propModel = new ExperimentalLanguageSpecificInstrumentationPropertyModel();
        props.put(name, propModel);
      }

      // Only set defaults for properties not already present in the model
      for (Map.Entry<String, String> defaultEntry : defaults.entrySet()) {
        propModel
            .getAdditionalProperties()
            .putIfAbsent(defaultEntry.getKey(), defaultEntry.getValue());
      }
    }

    return model;
  }

  public static class InstrumentationPropertyDefaults {

    private final Map<String, String> defaults = new LinkedHashMap<>();

    /** Set a default value for a property. */
    public InstrumentationPropertyDefaults setDefault(String key, String value) {
      defaults.put(key, value);
      return this;
    }

    Map<String, String> getDefaults() {
      return defaults;
    }
  }
}
