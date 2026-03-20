/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import com.grafana.extensions.filter.MetricsCustomizer;
import com.grafana.extensions.instrumentations.TestedInstrumentationsCustomizer;
import com.grafana.extensions.resources.ResourceCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

public class GrafanaAutoConfigCustomizerProvider implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
        .addPropertiesSupplier(GrafanaDistributionConfig.DEFAULTS::toConfigProperties)
        .addPropertiesCustomizer(TestedInstrumentationsCustomizer::customizeProperties)
        .addMeterProviderCustomizer(MetricsCustomizer::configure)
        .addResourceCustomizer(ResourceCustomizer::truncate);
  }
}
