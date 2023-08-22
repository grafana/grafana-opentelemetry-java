/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProductJpaRepository extends ReactiveCrudRepository<Product, Long> {}
