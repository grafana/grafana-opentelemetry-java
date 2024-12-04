/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.List;

public class DeferredSampler implements Sampler {

  private static final Sampler FROM_PARENT = Sampler.parentBased(Sampler.alwaysOff());

  public static Sampler configure(Sampler configured, ConfigProperties configProperties) {
    return new DeferredSampler();
  }

  @Override
  public String getDescription() {
    return "DeferredSampler";
  }

  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    SamplingDecision parentDecision =
        FROM_PARENT
            .shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks)
            .getDecision();
    if (SamplingDecision.RECORD_AND_SAMPLE.equals(parentDecision)) {
      // todo: fix propagation
      //      DynamicSampler.setSampled(traceId);
    }

    // always return true - because a child span might be sampled even if the parent is not
    // todo: fix propagation
    //    return SamplingResult.recordOnly();
    return SamplingResult.recordAndSample();
  }
}
