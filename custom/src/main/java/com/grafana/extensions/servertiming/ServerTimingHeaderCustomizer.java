/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.bootstrap.http.HttpServerResponseCustomizer;
import io.opentelemetry.javaagent.bootstrap.http.HttpServerResponseMutator;

/**
 * Adds {@code Server-Timing} header (and {@code Access-Control-Expose-Headers}) to the HTTP
 * response. The {@code Server-Timing} header contains the traceId and spanId of the server span.
 */
public class ServerTimingHeaderCustomizer implements HttpServerResponseCustomizer {
  static final String SERVER_TIMING = "Server-Timing";
  static final String EXPOSE_HEADERS = "Access-Control-Expose-Headers";

  // not using volatile because this field is set only once during agent initialization
  static boolean enabled = false;

  @Override
  public <RESPONSE> void customize(
      Context context, RESPONSE response, HttpServerResponseMutator<RESPONSE> responseMutator) {
    if (!enabled || !Span.fromContext(context).getSpanContext().isValid()) {
      return;
    }

    responseMutator.appendHeader(response, SERVER_TIMING, toHeaderValue(context));
    responseMutator.appendHeader(response, EXPOSE_HEADERS, SERVER_TIMING);
  }

  private static String toHeaderValue(Context context) {
    TraceParentHolder traceParentHolder = new TraceParentHolder();
    W3CTraceContextPropagator.getInstance()
        .inject(context, traceParentHolder, TraceParentHolder::set);
    return "traceparent;desc=\"" + traceParentHolder.traceParent + "\"";
  }

  private static class TraceParentHolder {
    String traceParent;

    public void set(String key, String value) {
      if ("traceparent".equals(key)) {
        traceParent = value;
      }
    }
  }
}
