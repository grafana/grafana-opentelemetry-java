/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CustomerMongoRepository extends ReactiveMongoRepository<Customer, String> {
  Flux<Customer> findByFirstName(String firstName);
}
