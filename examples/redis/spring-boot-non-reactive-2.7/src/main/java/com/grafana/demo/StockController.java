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

  private final JedisPooled jedis = new JedisPooled("redis", 6379);

  @GetMapping("/stock")
  public String getStock() {
    return String.join("", jedis.keys("hel?lo"));
  }
}
