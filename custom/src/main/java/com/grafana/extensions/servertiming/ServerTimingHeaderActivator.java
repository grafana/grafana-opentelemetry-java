/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static io.opentelemetry.sdk.autoconfigure.AutoConfigureUtil.getConfig;

import io.opentelemetry.javaagent.extension.AgentListener;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

public class ServerTimingHeaderActivator implements AgentListener {
  private static final String EMIT_RESPONSE_HEADERS = "grafana.otel.trace-response-header.enabled";

  @Override
  public void afterAgent(AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk) {
    ConfigProperties config = getConfig(autoConfiguredOpenTelemetrySdk);
    if (config.getBoolean(EMIT_RESPONSE_HEADERS, true)) {
      ServerTimingHeaderCustomizer.enabled = true;
    }
  }
}
