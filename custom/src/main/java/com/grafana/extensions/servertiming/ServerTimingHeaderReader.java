/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import com.grafana.extensions.sampler.DynamicSampler;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.instrumentation.api.semconv.http.HttpClientResponseConsumer;
import io.opentelemetry.instrumentation.api.semconv.http.HttpCommonAttributesGetter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ServerTimingHeaderReader implements HttpClientResponseConsumer {
  private static final TextMapGetter<Map<String, String>> GETTER =
      new TextMapGetter<Map<String, String>>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
          return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  @Override
  public <REQUEST, RESPONSE> void consume(
      HttpCommonAttributesGetter<REQUEST, RESPONSE> getter, REQUEST request, RESPONSE response) {
    List<String> timings =
        getter.getHttpResponseHeader(request, response, ServerTimingHeaderCustomizer.SERVER_TIMING);

    for (String timing : timings) {
      if (timing.startsWith("traceparent")) {
        String[] parts = timing.split(";");
        for (String part : parts) {
          if (part.startsWith("desc=")) {
            String traceParent = part.substring(6, part.length() - 1);
            Context traceparent =
                W3CTraceContextPropagator.getInstance()
                    .extract(
                        Context.current(),
                        Collections.singletonMap("traceparent", traceParent),
                        GETTER);
            SpanContext spanContext = Span.fromContext(traceparent).getSpanContext();
            if (spanContext.getTraceFlags().isSampled()) {
              DynamicSampler.getInstance().setSampled(spanContext.getTraceId(), "child");
            }
          }
        }
      }
    }
  }
}
