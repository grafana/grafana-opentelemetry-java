/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static com.grafana.extensions.servertiming.ServerTimingHeaderCustomizer.EXPOSE_HEADERS;
import static com.grafana.extensions.servertiming.ServerTimingHeaderCustomizer.SERVER_TIMING;
import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.sampler.DynamicSampler;
import com.grafana.extensions.sampler.SampleReason;
import com.grafana.extensions.sampler.SpanNameStats;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import java.time.Clock;
import java.time.Duration;
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

  @BeforeAll
  static void initialize() {
    DynamicSampler.configure(
        DefaultConfigProperties.createFromMap(Collections.emptyMap()), Clock.systemUTC());
  }

  @BeforeEach
  void setUp() {
    ServerTimingHeaderCustomizer.enabled = true;
    DynamicSampler.getInstance().resetForTest();
  }

  @Test
  void shouldNotSetAnyHeadersWithoutValidCurrentSpan() {
    var headers = new HashMap<String, String>();

    serverTiming.customize(Context.root(), headers, Map::put);

    assertThat(headers).isEmpty();
  }

  @Test
  void shouldSetHeaders() {
    SpanNameStats stats = SpanNameStats.getPrepopulatedForTest(Duration.ofMinutes(1), 11_900_000);
    DynamicSampler.getInstance().setStats("server", stats);
    assertSetHeader("00", span -> {});
    assertSetHeader(
        "01",
        span -> {
          DynamicSampler.getInstance().registerNewSpan((ReadableSpan) Span.current());
          DynamicSampler.getInstance()
              .setSampled(span.getSpanContext().getTraceId(), SampleReason.create("test"));
          DynamicSampler.getInstance().evaluateSampled((ReadWriteSpan) span);
        });
  }

  private void assertSetHeader(String traceFlags, Consumer<Span> spanConsumer) {
    var headers = new HashMap<String, String>();

    var spanContext =
        testing.runWithSpan(
            "server",
            () -> {
              serverTiming.customize(Context.current(), headers, Map::put);
              spanConsumer.accept(Span.current());
              return Span.current().getSpanContext();
            });

    if (traceFlags.equals("00")) {
      assertThat(headers).isEmpty();
      return;
    }
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
