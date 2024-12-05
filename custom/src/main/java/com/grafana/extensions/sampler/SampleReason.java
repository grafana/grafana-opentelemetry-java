/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

public class SampleReason {

  private static final AttributeKey<String> REASON = AttributeKey.stringKey("sampled.reason");

  public static Attributes create(String reason) {
    return Attributes.of(REASON, reason);
  }

  public static Attributes create(String reason, Attributes attributes) {
    return attributes.toBuilder().put(REASON, reason).build();
  }
}
