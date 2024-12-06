/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import java.util.Collection;

public class SamplingPropagator implements TextMapPropagator {
  private final TextMapPropagator delegate;

  public SamplingPropagator(TextMapPropagator delegate) {
    this.delegate = delegate;
  }

  public static TextMapPropagator configure(
      TextMapPropagator textMapPropagator, ConfigProperties configProperties) {
    return new SamplingPropagator(textMapPropagator);
  }

  public static <C> void injectWithDynamicSampleResult(
      Context context, C carrier, TextMapSetter<C> setter, TextMapPropagator delegate) {
    ReadWriteSpan span = (ReadWriteSpan) Span.fromContext(context);
    SpanContext spanContext = span.getSpanContext();
    boolean sampled = DynamicSampler.getInstance().evaluateSampled(span);
    delegate.inject(
        context.with(
            Span.wrap(
                SpanContext.create(
                    spanContext.getTraceId(),
                    spanContext.getSpanId(),
                    sampled ? TraceFlags.getSampled() : TraceFlags.getDefault(),
                    spanContext.getTraceState()))),
        carrier,
        setter);
  }

  @Override
  public Collection<String> fields() {
    return delegate.fields();
  }

  @Override
  public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
    injectWithDynamicSampleResult(context, carrier, setter, delegate);
  }

  @Override
  public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
    return delegate.extract(context, carrier, getter);
  }
}
