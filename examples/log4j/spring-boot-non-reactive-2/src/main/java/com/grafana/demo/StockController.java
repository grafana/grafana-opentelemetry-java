/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {

  private static final Logger LOG = LoggerFactory.getLogger(StockController.class);

  @GetMapping("/stock")
  public String getStock() {
    LOG.info("hello LGTM");
    return "hello LGTM";
  }
}
