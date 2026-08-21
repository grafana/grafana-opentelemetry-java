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
    // trim all string attributes to the configured UTF-8 byte limit
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
                String truncated = truncateUtf8(s, limit);
                if (!truncated.equals(s)) {
                  builder.put((AttributeKey<? super String>) key, truncated);
                }
              }
            });

    return builder.build();
  }

  // Grafana Cloud limits resource attribute values by UTF-8 byte size, not code point count:
  // https://grafana.com/docs/grafana-cloud/send-data/otlp/otlp-format-considerations/
  private static String truncateUtf8(String value, int limit) {
    int bytes = 0;
    int index = 0;
    while (index < value.length()) {
      int codePoint = value.codePointAt(index);
      int codePointBytes = utf8Length(codePoint);
      if (codePointBytes > limit - bytes) {
        break;
      }
      bytes += codePointBytes;
      // Advance by UTF-16 code units; UTF-8 bytes are counted separately above.
      index += Character.charCount(codePoint);
    }
    return index == value.length() ? value : value.substring(0, index);
  }

  private static int utf8Length(int codePoint) {
    if (codePoint <= 0x7F) {
      return 1;
    }
    if (codePoint <= 0x7FF) {
      return 2;
    }
    if (codePoint <= 0xFFFF) {
      return 3;
    }
    return 4;
  }
}
