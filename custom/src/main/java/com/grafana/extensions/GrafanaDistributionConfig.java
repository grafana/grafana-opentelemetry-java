/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import com.grafana.extensions.config.InstrumentationDefaults;

/** Shared configuration for the Grafana distribution, expressed in DC notation. */
class GrafanaDistributionConfig {

  static final InstrumentationDefaults DEFAULTS = new InstrumentationDefaults();

  static {
    DEFAULTS.getStructured("micrometer").setDefault("base_time_unit", "s");
    DEFAULTS.getStructured("log4j_appender").setDefault("experimental_log_attributes", "true");
    DEFAULTS.getStructured("logback_appender").setDefault("experimental_log_attributes", "true");
  }

  private GrafanaDistributionConfig() {}
}
