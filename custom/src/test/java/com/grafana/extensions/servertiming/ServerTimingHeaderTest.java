/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static com.grafana.extensions.servertiming.ServerTimingHeaderCustomizer.EXPOSE_HEADERS;
import static com.grafana.extensions.servertiming.ServerTimingHeaderCustomizer.SERVER_TIMING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ServerTimingHeaderTest {

  @RegisterExtension InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  private final ServerTimingHeaderCustomizer serverTiming = new ServerTimingHeaderCustomizer();

  @BeforeAll
  static void setUp() {
    ServerTimingHeaderCustomizer.enabled = true;
  }

  @Test
  void shouldNotSetAnyHeadersWithoutValidCurrentSpan() {
    // given
    var headers = new HashMap<String, String>();

    // when
    serverTiming.customize(Context.root(), headers, Map::put);

    // then
    assertTrue(headers.isEmpty());
  }

  @Test
  void shouldSetHeaders() {
    // given
    var headers = new HashMap<String, String>();

    // when
    var spanContext =
        testing.runWithSpan(
            "server",
            () -> {
              serverTiming.customize(Context.current(), headers, Map::put);
              return Span.current().getSpanContext();
            });

    // then
    assertEquals(2, headers.size());

    var serverTimingHeaderValue =
        "traceparent;desc=\"00-"
            + spanContext.getTraceId()
            + "-"
            + spanContext.getSpanId()
            + "-01\"";
    assertEquals(serverTimingHeaderValue, headers.get(SERVER_TIMING));
    assertEquals(SERVER_TIMING, headers.get(EXPOSE_HEADERS));
  }
}
