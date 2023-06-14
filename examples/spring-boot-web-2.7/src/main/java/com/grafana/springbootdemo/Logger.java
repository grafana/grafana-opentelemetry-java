package com.grafana.springbootdemo;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

//makes it easy to switch logging frameworks for testing
@Component
public class Logger {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(CustomerController.class);

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }
}
