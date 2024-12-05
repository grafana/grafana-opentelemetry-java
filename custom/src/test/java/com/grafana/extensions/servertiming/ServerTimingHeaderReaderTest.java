/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import static org.assertj.core.api.Assertions.assertThat;

import com.grafana.extensions.sampler.DynamicSampler;
import com.grafana.extensions.util.MovingAverage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.LibraryInstrumentationExtension;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ServerTimingHeaderReaderTest {

  private ServerTimingHeaderReader serverTimingHeaderReader = new ServerTimingHeaderReader();

  @RegisterExtension InstrumentationExtension testing = LibraryInstrumentationExtension.create();

  @BeforeAll
  static void initialize() {
    DynamicSampler.configure(DefaultConfigProperties.createFromMap(Collections.emptyMap()));
  }

  @BeforeEach
  void setUp() {
    DynamicSampler.getInstance().clear();
  }

  @Test
  void notSampled() {
    String spanName = "server";
    MovingAverage testMovingAvg = MovingAverage.getPrepopulatedMovingAvgForTest(3, 11_900_000);
    DynamicSampler.getInstance().setMovingAvg(spanName, testMovingAvg);
    testing.runWithSpan(
        spanName,
        () -> {
          String traceId = Span.current().getSpanContext().getTraceId();
          String serverTiming = ServerTimingHeaderCustomizer.toHeaderValue(Context.current());
          serverTimingHeaderReader.consume(
              new StringHttpCommonAttributesGetter(serverTiming), "request", "response");
          System.out.println(serverTiming);
          assertThat(DynamicSampler.getInstance().getSampledTraces()).doesNotContain(traceId);
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
