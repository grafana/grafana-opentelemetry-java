/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import com.grafana.extensions.resources.internal.DistroComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationCustomizer;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationCustomizerProvider;

public class GrafanaDeclarativeConfigurationCustomizerProvider
    implements DeclarativeConfigurationCustomizerProvider {

  @Override
  public void customize(DeclarativeConfigurationCustomizer customizer) {
    customizer.addModelCustomizer(
        model -> {
          DistroComponentProvider.addDistroResourceProvider(model);
          DeclarativeConfigPropertyCustomizer.addProperties(
              model, GrafanaAutoConfigCustomizerProvider.getDefaultProperties());
          return model;
        });
  }
}
