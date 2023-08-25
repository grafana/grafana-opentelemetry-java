/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class StockController {

  private final ReactiveRedisOperations<String, Coffee> coffeeOps;

  public StockController(ReactiveRedisOperations<String, Coffee> coffeeOps) {
    this.coffeeOps = coffeeOps;
  }

  @GetMapping("/stock")
  public Mono<String> getStock() {
    return coffeeOps.keys("*").flatMap(coffeeOps.opsForValue()::get).next().map(Coffee::getName);
  }
}
