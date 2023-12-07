/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class CartClient {

  private final Random random = new Random();

  private final LongCounter counter =
      GlobalOpenTelemetry.get().getMeter("application").counterBuilder("cart_client").build();

  @WithSpan("get_cart")
  public String getCart() {
    counter.add(1);
    if (random.nextBoolean()) {
      throw new RuntimeException("Failed to get cart");
    }
    return "cart";
  }
}
