/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;

public class ResourceCustomizer {

  static final String TRUNCATE_LENGTH = "grafana.otel.resource.attribute.value.length.limit";

  private ResourceCustomizer() {}

  @SuppressWarnings("unchecked")
  public static Resource truncate(Resource resource, ConfigProperties config) {
    // trim all the attributes according to the config
    int limit = config.getInt(TRUNCATE_LENGTH, 2048);
    if (limit <= 0) {
      return resource;
    }

    ResourceBuilder builder = resource.toBuilder();
    resource
        .getAttributes()
        .forEach(
            (key, value) -> {
              if (value instanceof String) {
                String s = (String) value;
                if (s.length() > limit) {
                  builder.put((AttributeKey<? super String>) key, s.substring(0, limit));
                }
              }
            });

    return builder.build();
  }
}
