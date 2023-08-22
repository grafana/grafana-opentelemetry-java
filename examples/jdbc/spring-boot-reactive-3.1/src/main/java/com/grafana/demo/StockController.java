/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StockController {
  private final ProductJpaRepository productJpaRepository;

  public StockController(ProductJpaRepository productJpaRepository) {
    this.productJpaRepository = productJpaRepository;
  }

  @GetMapping("/stock")
  public Mono<String> getStock() {
    return productJpaRepository.findById(1L).map(Product::getName);
  }
}
