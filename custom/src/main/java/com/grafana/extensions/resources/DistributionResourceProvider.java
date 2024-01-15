/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

public class DistributionResourceProvider implements ResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    return DistributionResource.get();
  }

  @Override
  public int order() {
    // make sure we have a higher priority than the default resource provider for otel.distro.name
    // and otel.distro.version
    return Integer.MAX_VALUE;
  }
}
