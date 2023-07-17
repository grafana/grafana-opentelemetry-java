/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

@AutoService(ResourceProvider.class)
public class DistributionResourceProvider implements ResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    try {
      Class.forName("jdk.jshell.JShell");
      System.out.println("USING JDK 9+");
    } catch (ClassNotFoundException e) {
      System.out.println("USING JDK 8");
    }

    // java.util.Map.of();
    return DistributionResource.get();
  }
}
