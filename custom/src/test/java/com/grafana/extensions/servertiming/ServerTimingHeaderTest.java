/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static com.grafana.extensions.servertiming.ServerTimingHeaderCustomizer.EXPOSE_HEADERS;
import static com.grafana.extensions.servertiming.ServerTimingHeaderCustomizer.SERVER_TIMING;
import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.sampler.DynamicSampler;
import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ServerTimingHeaderTest {

  @RegisterExtension InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  private final ServerTimingHeaderCustomizer serverTiming = new ServerTimingHeaderCustomizer();

  record TestCase(Resource want, ConfigProperties config) {}

  @BeforeAll
  static void initialize() {
    DynamicSampler.configure(DefaultConfigProperties.createFromMap(Collections.emptyMap()));
  }

  @BeforeEach
  void setUp() {
    ServerTimingHeaderCustomizer.enabled = true;
    DynamicSampler.getInstance().clear();
  }

  @Test
  void shouldNotSetAnyHeadersWithoutValidCurrentSpan() {
    var headers = new HashMap<String, String>();

    serverTiming.customize(Context.root(), headers, Map::put);

    assertThat(headers).isEmpty();
  }

  @Test
  void shouldSetHeaders() {
    MovingAverage testMovingAvg = MovingAverage.getPrepopulatedMovingAvgForTest(3, 11_900_000);
    DynamicSampler.getInstance().setMovingAvg("server", testMovingAvg);
    assertSetHeader("00", span -> {});
    // todo: fix propagation
    //    assertSetHeader(
        "01", span ->
    // DynamicSampler.getInstance().setSampled(span.getSpanContext().getTraceId()));
  }

  private void assertSetHeader(String traceFlags, Consumer<Span> spanConsumer) {
    var headers = new HashMap<String, String>();

    var spanContext =
        testing.runWithSpan(
            "server",
            () -> {
              spanConsumer.accept(Span.current());
              serverTiming.customize(Context.current(), headers, Map::put);
              return Span.current().getSpanContext();
            });

    assertThat(headers).hasSize(2);

    var serverTimingHeaderValue =
        "traceparent;desc=\"00-"
            + spanContext.getTraceId()
            + "-"
            + spanContext.getSpanId()
            + "-"
            + traceFlags
            + "\"";

    assertThat(headers)
        .containsEntry(SERVER_TIMING, serverTimingHeaderValue)
        .containsEntry(EXPOSE_HEADERS, SERVER_TIMING);
  }
}
