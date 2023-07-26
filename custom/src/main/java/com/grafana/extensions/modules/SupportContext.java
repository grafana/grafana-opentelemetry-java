/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import com.grafana.extensions.resources.internal.DistributionVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SupportContext {

  private static final Logger logger = Logger.getLogger(SupportContext.class.getName());

  private boolean enableUnsupportedInstrumentations;
  private boolean enableAllInstrumentations;
  private final List<String> enabledUnsupportedInstrumentations = new ArrayList<>();
  private final List<String> disabledInstrumentations = new ArrayList<>();

  public boolean isEnableUnsupportedInstrumentations() {
    return enableUnsupportedInstrumentations;
  }

  public void setEnableUnsupportedInstrumentations(boolean enableUnsupportedInstrumentations) {
    this.enableUnsupportedInstrumentations = enableUnsupportedInstrumentations;
  }

  public void setEnableAllInstrumentations(boolean enableAllInstrumentations) {
    this.enableAllInstrumentations = enableAllInstrumentations;
  }

  public List<String> getEnabledUnsupportedInstrumentations() {
    return enabledUnsupportedInstrumentations;
  }

  public List<String> getDisabledInstrumentations() {
    return disabledInstrumentations;
  }

  public void print() {
    if (enableUnsupportedInstrumentations
        && enabledUnsupportedInstrumentations.isEmpty()
        && disabledInstrumentations.isEmpty()
        && !enableAllInstrumentations) {
      // nothing happened
      enableUnsupportedInstrumentations = false;
    }

    String supportWarning =
        enableUnsupportedInstrumentations
            ? " (The javaagent is running in unsupported mode, "
                + "please remove the -Dgrafana.otel.instrumentation.enable-unsupported-modules=true command line "
                + "argument or GRAFANA_OTEL_INSTRUMENTATION_ENABLE_UNSUPPORTED_MODULES=true environment variable to turn on "
                + "the supported mode)"
            : "";

    logger.info(
        String.format(
            "Grafana OpenTelemetry Javaagent%s: version=%s, enableAllInstrumentations=%b, "
                + "enabledUnsupportedInstrumentations=%s, disabledInstrumentations=%s%s",
            enableUnsupportedInstrumentations ? " is UNSUPPORTED" : "",
            DistributionVersion.VERSION,
            enableAllInstrumentations,
            enabledUnsupportedInstrumentations,
            disabledInstrumentations,
            supportWarning));
  }
}
