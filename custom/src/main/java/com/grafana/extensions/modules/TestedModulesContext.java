/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import com.grafana.extensions.resources.internal.DistributionVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TestedModulesContext {

  private static final Logger logger = Logger.getLogger(TestedModulesContext.class.getName());

  private boolean excludeUntestedInstrumentations;
  private boolean includeAllInstrumentations;
  private final List<String> includedUntestedInstrumentations = new ArrayList<>();
  private final List<String> excludedInstrumentations = new ArrayList<>();

  public boolean isExcludeUntestedInstrumentations() {
    return excludeUntestedInstrumentations;
  }

  public void setExcludeUntestedInstrumentations(boolean excludeUntestedInstrumentations) {
    this.excludeUntestedInstrumentations = excludeUntestedInstrumentations;
  }

  public void setIncludeAllInstrumentations(boolean includeAllInstrumentations) {
    this.includeAllInstrumentations = includeAllInstrumentations;
  }

  public List<String> getIncludedUntestedInstrumentations() {
    return includedUntestedInstrumentations;
  }

  public List<String> getExcludedInstrumentations() {
    return excludedInstrumentations;
  }

  public void print() {
    if (excludeUntestedInstrumentations
      && includedUntestedInstrumentations.isEmpty()
      && excludedInstrumentations.isEmpty()
      && !includeAllInstrumentations) {
      // nothing happened
      excludeUntestedInstrumentations = false;
    }

    logger.info(
      String.format(
        "Grafana OpenTelemetry Javaagent: version=%s, includeAllInstrumentations=%b, "
          + "includedUntestedInstrumentations=%s, excludedInstrumentations=%s",
        DistributionVersion.VERSION,
        includeAllInstrumentations,
        includedUntestedInstrumentations,
        excludedInstrumentations));
  }
}
