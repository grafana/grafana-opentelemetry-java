/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class StockController {

  private final CustomerMongoRepository repository;

  public StockController(CustomerMongoRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/stock")
  public Flux<Customer> getStock() {
    return repository.findByFirstName("LGTM");
  }
}
