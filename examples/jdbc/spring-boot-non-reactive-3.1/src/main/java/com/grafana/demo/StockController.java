package com.grafana.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {
	private final ProductJpaRepository productJpaRepository;

	public StockController(ProductJpaRepository productJpaRepository) {
		this.productJpaRepository = productJpaRepository;
	}

	@GetMapping("/stock")
	public String getStock() {
		return "product found: " + productJpaRepository.findById(1L);
	}
}
