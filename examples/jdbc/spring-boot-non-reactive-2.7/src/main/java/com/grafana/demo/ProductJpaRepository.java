package com.grafana.demo;

import org.springframework.data.repository.CrudRepository;

public interface ProductJpaRepository extends CrudRepository<Product, Long> {
}
