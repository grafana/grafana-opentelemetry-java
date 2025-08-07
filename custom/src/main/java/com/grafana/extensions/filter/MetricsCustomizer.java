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

    // must be last, because it drops all metrics not explicitly allowed
    MetricFilter.configure(sdkMeterProviderBuilder, properties);

    return sdkMeterProviderBuilder;
  }
}
