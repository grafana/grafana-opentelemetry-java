/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

import java.util.Map;

@AutoService(ResourceProvider.class)
public class DistributionResourceProvider implements ResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    Map.of(); //should fail when testing against JDK 8
    return DistributionResource.get();
  }
}
