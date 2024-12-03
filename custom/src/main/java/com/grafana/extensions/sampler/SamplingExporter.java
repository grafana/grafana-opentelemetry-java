/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collection;

public class SamplingExporter implements SpanExporter {

  private final SpanExporter delegate;

  public SamplingExporter(SpanExporter delegate) {
    this.delegate = delegate;
  }

  public static SpanExporter configure(SpanExporter delegate, ConfigProperties properties) {
    return new SamplingExporter(delegate);
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> collection) {
    ArrayList<SpanData> export = new ArrayList<>();
    for (SpanData data : collection) {
      if (DynamicSampler.isSampled(data.getSpanContext().getTraceId())) {
        export.add(data);
      }
    }
    DynamicSampler.clear();
    return delegate.export(export);
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public void close() {
    delegate.close();
  }
}
