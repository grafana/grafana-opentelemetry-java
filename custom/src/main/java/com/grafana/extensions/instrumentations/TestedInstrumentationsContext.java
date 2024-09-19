/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.instrumentations;

import com.grafana.extensions.VersionLogger;
import com.grafana.extensions.resources.internal.DistributionVersion;
import java.util.ArrayList;
import java.util.List;

public class TestedInstrumentationsContext {

  private boolean useTestedInstrumentations;
  private boolean includeAllInstrumentations = true;
  private final List<String> includedUntestedInstrumentations = new ArrayList<>();
  private final List<String> excludedInstrumentations = new ArrayList<>();

  public TestedInstrumentationsContext(boolean useTestedInstrumentations) {
    this.useTestedInstrumentations = useTestedInstrumentations;
  }

  public boolean isUseTestedInstrumentations() {
    return useTestedInstrumentations;
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
    VersionLogger.logger.info(
        String.format(
            "Grafana OpenTelemetry Javaagent: version=%s, "
                + "includeAllInstrumentations=%b, useTestedInstrumentations=%b, "
                + "includedUntestedInstrumentations=%s, excludedInstrumentations=%s",
            DistributionVersion.VERSION,
            includeAllInstrumentations,
            useTestedInstrumentations,
            includedUntestedInstrumentations,
            excludedInstrumentations));
  }
}
