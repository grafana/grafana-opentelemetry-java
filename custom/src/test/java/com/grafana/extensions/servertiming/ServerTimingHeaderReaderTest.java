/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.sampler.DynamicSampler;
import com.grafana.extensions.sampler.SpanNameStats;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.trace.ReadableSpan;
import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Disabled("header reader is not working")
class ServerTimingHeaderReaderTest {

  private ServerTimingHeaderReader serverTimingHeaderReader = new ServerTimingHeaderReader();

  @RegisterExtension InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  @BeforeAll
  static void initialize() {
    DynamicSampler.configure(
        DefaultConfigProperties.createFromMap(Collections.emptyMap()), Clock.systemUTC());
  }

  @BeforeEach
  void setUp() {
    DynamicSampler.getInstance().resetForTest();
  }

  @Test
  void notSampled() {
    String spanName = "server";
    SpanNameStats stats = SpanNameStats.getPrepopulatedForTest(Duration.ofMinutes(1), 11_900_000);
    DynamicSampler.getInstance().setStats(spanName, stats);
    testing.runWithSpan(
        spanName,
        () -> {
          DynamicSampler.getInstance().registerNewSpan((ReadableSpan) Span.current());
          String traceId = Span.current().getSpanContext().getTraceId();
          String serverTiming = ServerTimingHeaderCustomizer.toHeaderValue(Context.current());
          serverTimingHeaderReader.consume(
              new StringHttpCommonAttributesGetter(serverTiming), "request", "response");
          assertThat(DynamicSampler.getInstance().getSampledTraces()).doesNotContain(traceId);
        });
  }

  @Test
  void sampled() {
    testing.runWithSpan(
        "server",
        () -> {
          DynamicSampler.getInstance().registerNewSpan((ReadableSpan) Span.current());
          String traceId = Span.current().getSpanContext().getTraceId();
          DynamicSampler.getInstance().setSampled(traceId, "test");
          String serverTiming = ServerTimingHeaderCustomizer.toHeaderValue(Context.current());

          // remove the traceId to see that it is added back by the reader
          DynamicSampler.getInstance().resetForTest();
          serverTimingHeaderReader.consume(
              new StringHttpCommonAttributesGetter(serverTiming), "request", "response");
          assertThat(DynamicSampler.getInstance().getSampledTraces()).contains(traceId);
        });
  }
}
