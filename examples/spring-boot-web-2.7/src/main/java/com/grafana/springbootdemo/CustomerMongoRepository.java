/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.springbootdemo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerMongoRepository extends MongoRepository<Customer, String> {

  Customer findByFirstName(String firstName);
}
