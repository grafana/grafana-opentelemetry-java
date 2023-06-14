package com.grafana.springbootdemo;

import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;

@RestController
public class StockController {

    private final ProductJpaRepository productJpaRepository;

    private final ReactiveRedisOperations<String, Coffee> coffeeOps;

    private final Random random = new Random();

    public StockController(ProductJpaRepository productJpaRepository, ReactiveRedisOperations<String, Coffee> coffeeOps) {
        this.productJpaRepository = productJpaRepository;
        this.coffeeOps = coffeeOps;
    }

    @GetMapping("/stock")
    public Mono<String> getStock() {
        if (random.nextDouble() < 0.3) {
            throw new RuntimeException("simulation error");
        }

        return Flux.merge(
                coffeeOps.keys("*").flatMap(coffeeOps.opsForValue()::get).next().map(Coffee::getName),
                productJpaRepository.findById(1L).map(Product::getName)
        ).reduce((c, p) -> c + " " + p);
    }
}
