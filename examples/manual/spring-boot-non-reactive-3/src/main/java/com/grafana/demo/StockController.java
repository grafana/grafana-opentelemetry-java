/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {

  private final CartClient cartClient;

  public StockController(CartClient cartClient) {
    this.cartClient = cartClient;
  }

  @GetMapping("/stock")
  public String getStock() {
    return cartClient.getCart();
  }
}
