/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;

public class MetricsCustomizer {

  private MetricsCustomizer() {}

  public static SdkMeterProviderBuilder configure(
      SdkMeterProviderBuilder sdkMeterProviderBuilder, ConfigProperties properties) {

    ServerAddressConfig.configure(sdkMeterProviderBuilder, properties);

    // Data Saver metric filtering is applied at export time via MetricFilter.configure, wired as a
    // MetricExporter customizer, so that retained instruments keep their upstream attribute advice
    // and default aggregation.

    return sdkMeterProviderBuilder;
  }
}
