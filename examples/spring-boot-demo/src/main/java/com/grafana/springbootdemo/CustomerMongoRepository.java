package com.grafana.springbootdemo;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerMongoRepository extends MongoRepository<Customer, String> {

  Customer findByFirstName(String firstName);

}
