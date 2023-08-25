# Overview

This demo project is a variation of the "spring-boot-web-3.1" project - but for
Spring Boot Web 2.7, with Java 8.

It is run exactly in the same way - just use a JDK 8 ("asdf" will pick the JDK automatically).

## Log4j

If you want to test log4j, change the following in "build.gradle":
- add `implementation "org.springframework.boot:spring-boot-starter-log4j2"` in the dependencies section
- add ```configurations {
  all*.exclude module: 'spring-boot-starter-logging'
} ```
