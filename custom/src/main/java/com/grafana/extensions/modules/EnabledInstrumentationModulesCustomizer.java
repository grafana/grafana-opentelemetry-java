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
public class EnabledInstrumentationModulesCustomizer {

  public static final String ENABLE_UNSUPPORTED_MODULES_PROPERTY =
      "grafana.otel.instrumentation.enable-unsupported-modules";

  private static final String DEFAULT_ENABLED_MODULE = "common.default";
  public static final String DEFAULT_ENABLED = "otel.instrumentation.common.default.enabled";

  private static final Logger logger =
      Logger.getLogger(EnabledInstrumentationModulesCustomizer.class.getName());
  public static final String USE_UNSUPPORTED_MODE_HINT =
      "(set grafana.otel.instrumentation.enable-unsupported-modules=true to remove this restriction)";

  public static Map<String, String> getDefaultProperties() {
    Map<String, String> updates = new HashMap<>();
    updates.put(DEFAULT_ENABLED, "false");

    for (String supportedModule : InstrumentationModules.SUPPORTED_MODULES) {
      updates.put(getEnabledProperty(supportedModule), "true");
    }

    return updates;
  }

  public static Map<String, String> customizeProperties(ConfigProperties configs) {
    boolean enableUnsupportedInstrumentations =
        configs.getBoolean(ENABLE_UNSUPPORTED_MODULES_PROPERTY, false);
    SupportContext supportContext = new SupportContext();
    supportContext.setEnableUnsupportedInstrumentations(enableUnsupportedInstrumentations);

    try {
      Set<String> supported =
          InstrumentationModules.SUPPORTED_MODULES.stream()
              .map(ConfigUtil::normalizePropertyKey)
              .collect(Collectors.toSet());

      return getAllProperties(configs).entrySet().stream()
          .flatMap(
              entry ->
                  stream(
                      getInstrumentationName(entry.getKey())
                          .flatMap(
                              name ->
                                  maybeDisableUnsupportedModule(
                                      supportContext,
                                      supported,
                                      name,
                                      entry.getValue().equals("true")))))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } finally {
      supportContext.print();
    }
  }

  private static Optional<Map.Entry<String, String>> maybeDisableUnsupportedModule(
      SupportContext supportContext,
      Set<String> supported,
      String instrumentationName,
      boolean enabled) {
    boolean enableUnsupported = supportContext.isEnableUnsupportedInstrumentations();

    if (instrumentationName.equals(DEFAULT_ENABLED_MODULE)) {
      if (enableUnsupported || !enabled) {
        supportContext.setEnableAllInstrumentations(enabled);
        return Optional.empty();
      }

      logger.info(String.format("Disabling %s %s", DEFAULT_ENABLED, USE_UNSUPPORTED_MODE_HINT));
      return Optional.of(entry(DEFAULT_ENABLED, "false"));
    }

    if (!enabled) {
      if (enableUnsupported) {
        supportContext.getDisabledInstrumentations().add(instrumentationName);
        return Optional.empty();
      }
      logger.info(
          String.format(
              "Enabling module %s again %s ", instrumentationName, USE_UNSUPPORTED_MODE_HINT));
      return Optional.of(entry(getEnabledProperty(instrumentationName), "true"));
    }

    if (supported.contains(instrumentationName)) {
      // is already enabled by default - don't log it
      return Optional.empty();
    }

    if (enableUnsupported) {
      supportContext.getEnabledUnsupportedInstrumentations().add(instrumentationName);
      return Optional.empty();
    }
    logger.info(
        String.format(
            "Disabling unsupported module %s %s", instrumentationName, USE_UNSUPPORTED_MODE_HINT));
    return Optional.of(entry(getEnabledProperty(instrumentationName), "false"));
  }

  private static Map.Entry<String, String> entry(String key, String value) {
    return new AbstractMap.SimpleImmutableEntry<>(key, value);
  }

  private static <T> Stream<T> stream(Optional<T> optional) {
    return optional.map(Stream::of).orElseGet(Stream::empty);
  }

  private static String getEnabledProperty(String name) {
    return "otel.instrumentation." + name + ".enabled";
  }

  private static Optional<String> getInstrumentationName(String property) {
    if (property.startsWith("otel.instrumentation.") && property.endsWith(".enabled")) {
      return Optional.of(
          property.substring(
              property.indexOf("otel.instrumentation.") + 21, property.indexOf(".enabled")));
    } else {
      return Optional.empty();
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, String> getAllProperties(ConfigProperties configProperties) {
    return (Map<String, String>) FieldUtils.readDeclaredField(configProperties, "config");
  }
}
