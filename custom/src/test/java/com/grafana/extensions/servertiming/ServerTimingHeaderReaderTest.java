/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ServerTimingHeaderReaderTest {

  private ServerTimingHeaderReader serverTimingHeaderReader = new ServerTimingHeaderReader();

  @RegisterExtension InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  @BeforeEach
  void setUp() {
    ServerTimingHeaderCustomizer.sampledTraces.clear();
  }

  @Test
  void notSampled() {
    testing.runWithSpan(
        "server",
        () -> {
          String serverTiming = ServerTimingHeaderCustomizer.toHeaderValue(Context.current());

          serverTimingHeaderReader.consume(
              new StringHttpCommonAttributesGetter(serverTiming), "request", "response");
          assertThat(ServerTimingHeaderCustomizer.sampledTraces).isEmpty();
        });
  }

  @Test
  void sampled() {
    testing.runWithSpan(
        "server",
        () -> {
          String traceId = Span.current().getSpanContext().getTraceId();
          ServerTimingHeaderCustomizer.sampledTraces.add(traceId);
          String serverTiming = ServerTimingHeaderCustomizer.toHeaderValue(Context.current());

          // remove the traceId to see that it is added back by the reader
          ServerTimingHeaderCustomizer.sampledTraces.clear();
          serverTimingHeaderReader.consume(
              new StringHttpCommonAttributesGetter(serverTiming), "request", "response");
          assertThat(ServerTimingHeaderCustomizer.sampledTraces).contains(traceId);
        });
  }
}
