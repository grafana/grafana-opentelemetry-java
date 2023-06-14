package com.grafana.springbootdemo;

import org.springframework.data.repository.CrudRepository;

public interface ProductJpaRepository extends CrudRepository<Product, Long> {
}
