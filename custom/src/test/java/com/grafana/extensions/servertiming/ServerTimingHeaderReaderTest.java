/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.sampler.DynamicSampler;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Disabled("todo: fix propagation")
class ServerTimingHeaderReaderTest {

  private ServerTimingHeaderReader serverTimingHeaderReader = new ServerTimingHeaderReader();

  @RegisterExtension InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  @BeforeEach
  void setUp() {
    DynamicSampler.getInstance().clear();
  }

  @Test
  void notSampled() {
    testing.runWithSpan(
        "server",
        () -> {
          String serverTiming = ServerTimingHeaderCustomizer.toHeaderValue(Context.current());

          serverTimingHeaderReader.consume(
              new StringHttpCommonAttributesGetter(serverTiming), "request", "response");
          assertThat(DynamicSampler.getInstance().getSampledTraces()).isEmpty();
        });
  }

  @Test
  void sampled() {
    testing.runWithSpan(
        "server",
        () -> {
          String traceId = Span.current().getSpanContext().getTraceId();
          DynamicSampler.getInstance().setSampled(traceId);
          String serverTiming = ServerTimingHeaderCustomizer.toHeaderValue(Context.current());

          // remove the traceId to see that it is added back by the reader
          DynamicSampler.getInstance().clear();
          serverTimingHeaderReader.consume(
              new StringHttpCommonAttributesGetter(serverTiming), "request", "response");
          assertThat(DynamicSampler.getInstance().getSampledTraces()).contains(traceId);
        });
  }
}
