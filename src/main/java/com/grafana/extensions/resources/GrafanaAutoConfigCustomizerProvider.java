/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import java.util.logging.Logger;

/**
 * This is one of the main entry points for Instrumentation Agent's customizations. It allows
 * configuring the {@link AutoConfigurationCustomizer}. See the {@link
 * #customize(AutoConfigurationCustomizer)} method below.
 *
 * @see AutoConfigurationCustomizerProvider
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class GrafanaAutoConfigCustomizerProvider implements AutoConfigurationCustomizerProvider {

  private static final Logger logger =
      Logger.getLogger(GrafanaAutoConfigCustomizerProvider.class.getName());

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration
        .addPropertiesCustomizer(
            config -> LoggingExporterConfigCustomizer.getCustomProperties(config))
        .addPropertiesCustomizer(
            config -> GrafanaCloudConfigCustomizer.getCustomProperties(config));
  }
}
