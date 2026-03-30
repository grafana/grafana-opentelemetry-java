/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.filter;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import java.util.HashSet;
import java.util.Set;

public class ServerAddressConfig {
  public static final String SERVER_ADDRESS_OPT_IN =
      "grafana.otel.http-server-request-duration.server-attributes.enabled";

  private ServerAddressConfig() {}

  static void configure(
      SdkMeterProviderBuilder sdkMeterProviderBuilder, ConfigProperties properties) {
    if (!properties.getBoolean(SERVER_ADDRESS_OPT_IN, false)) {
      // The upstream instrumentation already sets the same attribute advice for the base 7
      // attributes. Registering a View here would shadow the MetricReader's
      // DefaultAggregationSelector (e.g. preventing exponential histogram aggregation).
      return;
    }

    // enable server.address and server.port dimensions - see
    // https://opentelemetry.io/docs/specs/semconv/http/http-metrics/#metric-httpserverrequestduration
    Set<String> keys = new HashSet<>();
    keys.add("http.route");
    keys.add("http.request.method");
    keys.add("http.response.status_code");
    keys.add("error.type");
    keys.add("network.protocol.name");
    keys.add("network.protocol.version");
    keys.add("url.scheme");
    keys.add("server.address");
    keys.add("server.port");

    ViewBuilder builder = View.builder();
    builder.setAttributeFilter(keys);

    sdkMeterProviderBuilder.registerView(
        InstrumentSelector.builder().setName("http.server.request.duration").build(),
        builder.build());
  }
}
