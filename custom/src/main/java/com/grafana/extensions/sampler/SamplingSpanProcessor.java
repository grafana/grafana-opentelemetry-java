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
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor;

public class SamplingSpanProcessor implements ExtendedSpanProcessor {
  private final ConfigProperties properties;

  public SamplingSpanProcessor(ConfigProperties properties) {
    this.properties = properties;
  }

  public static SdkTracerProviderBuilder configure(
      SdkTracerProviderBuilder sdkTracerProviderBuilder, ConfigProperties configProperties) {
    DynamicSampler.configure(configProperties);
    return sdkTracerProviderBuilder.addSpanProcessor(new SamplingSpanProcessor(configProperties));
  }

  @Override
  public void onStart(Context context, ReadWriteSpan readWriteSpan) {
    DynamicSampler.getInstance().registerNewSpan(readWriteSpan);
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan readableSpan) {}

  @Override
  public boolean isEndRequired() {
    return false;
  }

  @Override
  public void onEnding(ReadWriteSpan span) {
    DynamicSampler.getInstance().evaluateSampled(span);
  }

  @Override
  public boolean isOnEndingRequired() {
    return true;
  }
}
