/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_VERSION;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.grafana.extensions.resources.internal.DistributionVersion;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DistributionResourceProviderTest {

  @Test
  public void createResourceProvider() {
    DistributionResourceProvider provider = new DistributionResourceProvider();
    Resource r = provider.createResource(null);
    assertThat(r.getAttributes().size()).isEqualTo(2);
    Map<AttributeKey<?>, Object> attributesMap = r.getAttributes().asMap();
    assertThat(attributesMap)
        .isEqualTo(
            ImmutableMap.of(
                TELEMETRY_SDK_NAME, "grafana", TELEMETRY_SDK_VERSION, DistributionVersion.VERSION));
    String version = (String) attributesMap.get(ResourceAttributes.TELEMETRY_SDK_VERSION);
    assertThat(version).matches("(\\d+\\.)?\\d+\\.\\d+(-SNAPSHOT)?");
    assertThat(r.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
  }
}
