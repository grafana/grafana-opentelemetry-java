/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static io.opentelemetry.semconv.ResourceAttributes.TELEMETRY_SDK_NAME;
import static io.opentelemetry.semconv.ResourceAttributes.TELEMETRY_SDK_VERSION;

import com.grafana.extensions.resources.internal.DistributionVersion;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ResourceAttributes;

public final class DistributionResource {

  private static final Resource INSTANCE = buildResource();

  private DistributionResource() {}

  public static Resource get() {
    return INSTANCE;
  }

  static Resource buildResource() {
    return Resource.create(
        Attributes.of(
            TELEMETRY_SDK_NAME, "grafana", TELEMETRY_SDK_VERSION, DistributionVersion.VERSION),
        ResourceAttributes.SCHEMA_URL);
  }
}
