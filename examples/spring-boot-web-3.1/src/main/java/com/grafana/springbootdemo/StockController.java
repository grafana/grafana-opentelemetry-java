/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.springbootdemo;

import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPooled;

@RestController
public class StockController {

  private final ProductJpaRepository productJpaRepository;

  private final JedisPooled jedis = new JedisPooled("localhost", 6379);

  private final Random random = new Random();

  public StockController(ProductJpaRepository productJpaRepository) {
    this.productJpaRepository = productJpaRepository;
  }

  private static final Logger logger = LoggerFactory.getLogger(StockController.class);

  @GetMapping("/stock")
  public String getStock() {
    try {
      jedis.sadd("planets", "Venus");
    } catch (Exception e) {
      logger.warn("error connecting to redis", e);
    }

    if (random.nextDouble() < 0.3) {
      throw new RuntimeException("simulation error");
    }

    return "product found: " + product;
  }
}
