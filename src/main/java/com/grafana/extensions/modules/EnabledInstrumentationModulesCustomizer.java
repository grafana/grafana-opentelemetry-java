/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * <a
 * href="https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/b917b3bf9c16d7327208a9f17a8db6d1a746829e/javaagent-tooling/src/main/java/io/opentelemetry/javaagent/tooling/config/AgentConfig.java#L12-L28">This
 * is the relevant upstream method</a>
 */
public class EnabledInstrumentationModulesCustomizer {

  private static final String ENABLE_UNSUPPORTED_MODULES_PROPERTY =
      "grafana.otel.instrumentation.enable-unsupported-modules";

  public static final String DEFAULT_ENABLED_MODULE = "common.default";
  public static final String DEFAULT_ENABLED = "otel.instrumentation.common.default.enabled";

  private static final Logger logger =
      Logger.getLogger(EnabledInstrumentationModulesCustomizer.class.getName());

  public static Map<String, String> getDefaultProperties() {
    Map<String, String> m = new HashMap<>();
    m.put(DEFAULT_ENABLED, "false");

    for (String supportedModule : InstrumentationModules.SUPPORTED_MODULES) {
      m.put(getEnabledProperty(supportedModule), "true");
    }

    return m;
  }

  public static Map<String, String> customizeProperties(ConfigProperties configs) {
    if (configs.getBoolean(ENABLE_UNSUPPORTED_MODULES_PROPERTY, false)) {
      logger.info("Enabling unsupported modules");
      return Collections.emptyMap();
    }

    Set<String> supported =
        InstrumentationModules.SUPPORTED_MODULES.stream()
            .map(ConfigUtil::normalizePropertyKey)
            .collect(Collectors.toSet());

    return getAllProperties(configs).entrySet().stream()
        .filter(entry -> entry.getValue().equals("true")) // it's allowed to disable modules
        .flatMap(
            entry ->
                stream(
                    getInstrumentationName(entry.getKey())
                        .flatMap(name -> disableUnsupportedModule(supported, name))))
        .collect(Collectors.toMap(property -> property, property -> "false"));
  }

  private static Optional<String> disableUnsupportedModule(
      Set<String> supported, String instrumentationName) {
    if (supported.contains(instrumentationName)) {
      return Optional.empty();
    }

    if (instrumentationName.equals(DEFAULT_ENABLED_MODULE)) {
      logger.info(
          String.format(
              "Disabling %s (set grafana.otel.instrumentation.enable-unsupported-modules=true "
                  + "to be able to enable all modules)",
              DEFAULT_ENABLED));
      return Optional.of(DEFAULT_ENABLED);
    }

    logger.info(
        String.format(
            "Disabling unsupported module %s (set grafana.otel.instrumentation.enable-unsupported-modules=true "
                + "to enable unsupported modules)",
            instrumentationName));
    return Optional.of(getEnabledProperty(instrumentationName));
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
  static Map<String, String> getAllProperties(ConfigProperties configProperties) {
    try {
      return (Map<String, String>) FieldUtils.readDeclaredField(configProperties, "config", true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
