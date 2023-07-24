/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import com.google.auto.service.AutoService;
import com.grafana.extensions.modules.EnabledInstrumentationModulesCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;

@AutoService(AutoConfigurationCustomizerProvider.class)
public class GrafanaAutoConfigCustomizerProvider implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
        .addPropertiesSupplier(EnabledInstrumentationModulesCustomizer::getDefaultProperties)
        .addPropertiesCustomizer(EnabledInstrumentationModulesCustomizer::customizeProperties)
        .addPropertiesCustomizer(LoggingExporterConfigCustomizer::customizeProperties)
        .addPropertiesCustomizer(GrafanaCloudConfigCustomizer::customizeProperties);
  }
}
