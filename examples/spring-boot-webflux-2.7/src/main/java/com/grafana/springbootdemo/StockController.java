/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.springbootdemo;

import java.util.Random;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class StockController {

  private final ReactiveRedisOperations<String, Coffee> coffeeOps;

  private final Random random = new Random();

  public StockController(
      ProductJpaRepository productJpaRepository,
      ReactiveRedisOperations<String, Coffee> coffeeOps) {
    this.coffeeOps = coffeeOps;
  }

  @GetMapping("/stock")
  public Mono<String> getStock() {
    if (random.nextDouble() < 0.3) {
      throw new RuntimeException("simulation error");
    }

    return Flux.merge(
            coffeeOps.keys("*").flatMap(coffeeOps.opsForValue()::get).next().map(Coffee::getName),
            )
        .reduce((c, p) -> c + " " + p);
  }
}
