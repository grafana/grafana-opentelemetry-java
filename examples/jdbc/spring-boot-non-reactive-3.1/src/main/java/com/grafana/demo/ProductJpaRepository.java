/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

import org.springframework.data.repository.CrudRepository;

public interface ProductJpaRepository extends CrudRepository<Product, Long> {}
