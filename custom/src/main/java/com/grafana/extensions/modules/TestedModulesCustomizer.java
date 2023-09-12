/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import com.grafana.extensions.util.FieldUtils;
import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <a
 * href="https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/b917b3bf9c16d7327208a9f17a8db6d1a746829e/javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/config/AgentConfig.java#L12-L28">This
 * is the relevant upstream method</a>
 */
public class TestedModulesCustomizer {

  public static final String EXCLUDE_UNTESTED_MODULES_PROPERTY =
    "grafana.otel.instrumentation.exclude-untested-modules";

  private static final String DEFAULT_INCLUDED_MODULE = "common.default";
  public static final String DEFAULT_INCLUDED = "otel.instrumentation.common.default.included";

  private static final Logger logger =
    Logger.getLogger(TestedModulesCustomizer.class.getName());
  public static final String USE_UNTESTED_MODE_HINT =
    "(remove grafana.otel.instrumentation.exclude-untested-modules=true to remove this restriction)";

  public static Map<String, String> getDefaultProperties() {
    Map<String, String> updates = new HashMap<>();
    updates.put(DEFAULT_INCLUDED, "false");

    for (String module : InstrumentationModules.TESTED_MODULES) {
      updates.put(getIncludedProperty(module), "true");
    }

    return updates;
  }

  public static Map<String, String> customizeProperties(ConfigProperties configs) {
    boolean excludeUntestedInstrumentations =
      configs.getBoolean(EXCLUDE_UNTESTED_MODULES_PROPERTY, false);
    TestedModulesContext testedModulesContext = new TestedModulesContext();
    testedModulesContext.setExcludeUntestedInstrumentations(excludeUntestedInstrumentations);

    try {
      Set<String> tested =
        InstrumentationModules.TESTED_MODULES.stream()
          .map(ConfigUtil::normalizePropertyKey)
          .collect(Collectors.toSet());

      return getAllProperties(configs).entrySet().stream()
        .flatMap(
          entry ->
            stream(
              getInstrumentationName(entry.getKey())
                .flatMap(
                  name ->
                    maybeExcludeUntestedModule(
                      testedModulesContext,
                      tested,
                      name,
                      entry.getValue().equals("true")))))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } finally {
      testedModulesContext.print();
    }
  }

  private static Optional<Map.Entry<String, String>> maybeExcludeUntestedModule(
    TestedModulesContext testedModulesContext,
    Set<String> tested,
    String instrumentationName,
    boolean included) {
    boolean excludeUntested = testedModulesContext.isExcludeUntestedInstrumentations();

    if (instrumentationName.equals(DEFAULT_INCLUDED_MODULE)) {
      if (excludeUntested && included) {
        logger.info(String.format("Excluding %s %s", DEFAULT_INCLUDED, USE_UNTESTED_MODE_HINT));
        return Optional.of(entry(DEFAULT_INCLUDED, "false"));
      }
      testedModulesContext.setIncludeAllInstrumentations(included);
      return Optional.empty();
    }

    if (!included) {
      if (excludeUntested) {
        logger.info(
          String.format(
            "Including module %s again %s ", instrumentationName, USE_UNTESTED_MODE_HINT));
        return Optional.of(entry(getIncludedProperty(instrumentationName), "true"));
      }
      testedModulesContext.getExcludedInstrumentations().add(instrumentationName);
      return Optional.empty();
    }

    if (tested.contains(instrumentationName)) {
      // is already included by default - don't log it
      return Optional.empty();
    }

    if (excludeUntested) {
      logger.info(
        String.format(
          "Excluding untested module %s %s", instrumentationName, USE_UNTESTED_MODE_HINT));
      return Optional.of(entry(getIncludedProperty(instrumentationName), "false"));
    }
    testedModulesContext.getIncludedUntestedInstrumentations().add(instrumentationName);
    return Optional.empty();
  }

  private static Map.Entry<String, String> entry(String key, String value) {
    return new AbstractMap.SimpleImmutableEntry<>(key, value);
  }

  private static <T> Stream<T> stream(Optional<T> optional) {
    return optional.map(Stream::of).orElseGet(Stream::empty);
  }

  private static String getIncludedProperty(String name) {
    return "otel.instrumentation." + name + ".included";
  }

  private static Optional<String> getInstrumentationName(String property) {
    if (property.startsWith("otel.instrumentation.") && property.endsWith(".included")) {
      return Optional.of(
        property.substring(
          property.indexOf("otel.instrumentation.") + 21, property.indexOf(".included")));
    } else {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, String> getAllProperties(ConfigProperties configProperties) {
    return (Map<String, String>) FieldUtils.readDeclaredField(configProperties, "config");
  }
}
