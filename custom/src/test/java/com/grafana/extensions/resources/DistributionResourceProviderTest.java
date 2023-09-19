/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static io.opentelemetry.semconv.ResourceAttributes.TELEMETRY_SDK_NAME;
import static io.opentelemetry.semconv.ResourceAttributes.TELEMETRY_SDK_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.resources.internal.DistributionVersion;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ResourceAttributes;
import org.junit.jupiter.api.Test;

public class DistributionResourceProviderTest {

  @Test
  public void createResourceProvider() {
    DistributionResourceProvider provider = new DistributionResourceProvider();
    Resource r = provider.createResource(null);
    assertThat(r.getAttributes().asMap())
        .hasSize(2)
        .containsEntry(TELEMETRY_SDK_NAME, "grafana")
        .containsEntry(TELEMETRY_SDK_VERSION, DistributionVersion.VERSION);
    assertThat(r.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
  }
}
