/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.servertiming;

import io.opentelemetry.instrumentation.api.semconv.http.HttpCommonAttributesGetter;
import java.util.List;
import javax.annotation.Nullable;

class StringHttpCommonAttributesGetter implements HttpCommonAttributesGetter<String, String> {
  private String serverTiming;

  public StringHttpCommonAttributesGetter(String serverTiming) {
    this.serverTiming = serverTiming;
  }

  @Nullable
  @Override
  public String getHttpRequestMethod(String s) {
    return "";
  }

  @Override
  public List<String> getHttpRequestHeader(String s, String name) {
    return List.of();
  }

  @Nullable
  @Override
  public Integer getHttpResponseStatusCode(String s, String s2, @Nullable Throwable error) {
    return 0;
  }

  @Override
  public List<String> getHttpResponseHeader(String s, String s2, String name) {
    if (name.equals("Server-Timing")) {
      return List.of(serverTiming);
    }
    return List.of();
  }
}
