/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class SamplingSpanProcessor implements SpanProcessor {

  @Override
  public void onStart(Context context, ReadWriteSpan readWriteSpan) {}

  @Override
  public boolean isStartRequired() {
    return false;
  }

  @Override
  public void onEnd(ReadableSpan readableSpan) {
    DynamicSampler.evaluateSampled(readableSpan);
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  public static SdkTracerProviderBuilder configure(
      SdkTracerProviderBuilder sdkTracerProviderBuilder, ConfigProperties configProperties) {
    new DynamicSampler(configProperties);
    return sdkTracerProviderBuilder.addSpanProcessor(new SamplingSpanProcessor());
  }
}