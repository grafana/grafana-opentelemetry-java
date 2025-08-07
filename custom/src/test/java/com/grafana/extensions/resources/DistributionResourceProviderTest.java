/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.resources.internal.DistributionVersion;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

public class DistributionResourceProviderTest {

  @Test
  public void createResourceProvider() {
    DistributionResourceProvider provider = new DistributionResourceProvider();
    Resource r = provider.createResource(null);
    assertThat(r.getAttributes().asMap())
        .hasSize(2)
        .containsEntry(DistributionResource.DISTRIBUTION_NAME, "grafana-opentelemetry-java")
        .containsEntry(DistributionResource.DISTRIBUTION_VERSION, DistributionVersion.VERSION);
  }
}
