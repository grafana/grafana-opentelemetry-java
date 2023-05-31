/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#span-exporter">
 * OpenTelemetry Specification</a> for more information about {@link SpanExporter}.
 *
 * @see DemoAutoConfigurationCustomizerProvider
 */
public class DemoSpanExporter implements SpanExporter {
  private static final Logger logger = LoggerFactory.getLogger(DemoSpanExporter.class);

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    logger.info(String.format("%d spans exported", spans.size()));
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
