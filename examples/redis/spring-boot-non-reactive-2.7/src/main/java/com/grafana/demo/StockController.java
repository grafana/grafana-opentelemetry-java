/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPooled;

@RestController
public class StockController {

  private final JedisPooled jedis = new JedisPooled("localhost", 6379);

  @GetMapping("/stock")
  public String getStock() {
    jedis.sadd("planets", "Venus");
    return "Stock sent";
  }
}
