/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.instrumentations;

import static com.grafana.extensions.instrumentations.Instrumentations.TESTED_INSTRUMENTATIONS;

import com.grafana.extensions.util.FieldUtils;
import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.AbstractMap;
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
public class TestedInstrumentationsCustomizer {

  public static final String USE_TESTED_INSTRUMENTATIONS_PROPERTY =
      "grafana.otel.use-tested-instrumentations";
  public static final String ENABLED_PREFIX = "otel.instrumentation.";

  private static final String DEFAULT_INCLUDED_INSTRUMENTATION = "common.default";
  public static final String DEFAULT_INCLUDED = "otel.instrumentation.common.default.enabled";

  private static final Logger logger =
      Logger.getLogger(TestedInstrumentationsCustomizer.class.getName());
  public static final String USE_UNTESTED_MODE_HINT =
      "(remove grafana.otel.use-tested-instrumentations=true to remove this restriction)";
  public static final String ENABLED_SUFFIX = ".enabled";

  public static Map<String, String> customizeProperties(ConfigProperties configs) {
    TestedInstrumentationsContext context =
        new TestedInstrumentationsContext(
            configs.getBoolean(USE_TESTED_INSTRUMENTATIONS_PROPERTY, false));

    try {
      Set<String> tested =
          TESTED_INSTRUMENTATIONS.stream()
              .map(ConfigUtil::normalizePropertyKey)
              .collect(Collectors.toSet());

      Map<String, String> updates =
          getAllProperties(configs).entrySet().stream()
              .flatMap(
                  entry ->
                      stream(
                          getInstrumentationName(entry.getKey())
                              .flatMap(
                                  name ->
                                      maybeUseTestedInstrumentation(
                                          context, tested, name, entry.getValue().equals("true")))))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      if (context.isUseTestedInstrumentations()) {
        updates.put(DEFAULT_INCLUDED, "false");
        context.setIncludeAllInstrumentations(false);

        for (String instrumentation : TESTED_INSTRUMENTATIONS) {
          updates.put(getIncludedProperty(instrumentation), "true");
        }
      }

      return updates;
    } finally {
      context.print();
    }
  }

  private static Optional<Map.Entry<String, String>> maybeUseTestedInstrumentation(
      TestedInstrumentationsContext testedInstrumentationsContext,
      Set<String> tested,
      String instrumentationName,
      boolean included) {
    boolean useTested = testedInstrumentationsContext.isUseTestedInstrumentations();

    if (instrumentationName.equals(DEFAULT_INCLUDED_INSTRUMENTATION)) {
      if (useTested && included) {
        logger.info(String.format("Excluding %s %s", DEFAULT_INCLUDED, USE_UNTESTED_MODE_HINT));
        return Optional.of(entry(DEFAULT_INCLUDED, "false"));
      }
      testedInstrumentationsContext.setIncludeAllInstrumentations(included);
      return Optional.empty();
    }

    if (!included) {
      if (useTested) {
        logger.info(
            String.format(
                "Including instrumentation %s again %s ",
                instrumentationName, USE_UNTESTED_MODE_HINT));
        return Optional.of(entry(getIncludedProperty(instrumentationName), "true"));
      }
      testedInstrumentationsContext.getExcludedInstrumentations().add(instrumentationName);
      return Optional.empty();
    }

    if (tested.contains(instrumentationName)) {
      // is already included by default - don't log it
      return Optional.empty();
    }

    if (useTested) {
      logger.info(
          String.format(
              "Excluding untested instrumentation %s %s",
              instrumentationName, USE_UNTESTED_MODE_HINT));
      return Optional.of(entry(getIncludedProperty(instrumentationName), "false"));
    }
    testedInstrumentationsContext.getIncludedUntestedInstrumentations().add(instrumentationName);
    return Optional.empty();
  }

  private static Map.Entry<String, String> entry(String key, String value) {
    return new AbstractMap.SimpleImmutableEntry<>(key, value);
  }

  private static <T> Stream<T> stream(Optional<T> optional) {
    return optional.map(Stream::of).orElseGet(Stream::empty);
  }

  private static String getIncludedProperty(String name) {
    return ENABLED_PREFIX + name + ENABLED_SUFFIX;
  }

  private static Optional<String> getInstrumentationName(String property) {
    if (property.startsWith(ENABLED_PREFIX) && property.endsWith(ENABLED_SUFFIX)) {
      return Optional.of(
          property.substring(
              property.indexOf(ENABLED_PREFIX) + 21, property.indexOf(ENABLED_SUFFIX)));
    } else {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, String> getAllProperties(ConfigProperties configProperties) {
    return (Map<String, String>) FieldUtils.readDeclaredField(configProperties, "config");
  }
}
