# Overview

The Demo application is a Spring Boot application to test various instrumentation libraries.

The goal is to make it very easy to check if a certain combination of libraries and frameworks is observable
using the [Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation),
e.g. Java 8 with Active MQ.

This demo takes some ideas from https://github.com/grafana/opentelemetry-demo/ - but is very JVM focused and tries to be easier to install (although that can still be simplified further).

# Running

Most services are part of docker compose, so they are started with `docker-compose up`.
Everything else is mentioned below.

## Java

use https://asdf-vm.com/ to switch jdk versions - or just look at `.tool-versions` and select the JDK manually

## Grafana Agent

- Go to Grafana Home page
- Click on "Connect data"
- search for "OpenTelemetry (OTLP)"
- follow the instructions there

## OpenTelemetry Collector

(currently not recommended)

Instead of the Grafana Agent, you can also use the otel collector, which is a bit more complicated, but implements
the latest Prometheus naming conventions (which we're not using at the moment)

- Go to the directory where you checked out opentelemetry-demo (see [Kafka](#kafka))
- Fill out [these variables](https://github.com/grafana/opentelemetry-demo/blob/2e0e2ccf762749b5beef61df5cef48fd0c3a4de3/.env#L122C1-L126) in ".env"
- `docker-compose --env-file .env  up otelcol`

## Java Agent

- `cd examples/spring-boot-demo`
- `curl -Lo opentelemetry-javaagent-1.27.0.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar`

## Demo Application

- check that you have Java 17 or higher
- `../../gradlew bootRun`
- In another shell: `curl http://localhost:8080/cart`

# Architecture

The Demo application is a single Spring Boot application that calls itself using HTTP.

```
┌──────────────────────────┐
│                          │       ┌───────────────────┐
│                          ├──────►│ Kafka             │
│                          │       └───────────────────┘
│                          │
│ Cart Controller          │
│                          │
│                          │
│                          │
│                          │
└─────────────────┬────┬───┘      ┌────────────────────────────┐          ┌────────────────────┐
                  │    │          │                            ├─────────►│ IBM MQ / JMS       │
                  │    │          │                            │          └────────────────────┘
                  │    │ HTTP     │                            │
                  │    └─────────►│                            │
                  │               │  Customer Controller       │          ┌───────────────────┐
                  │               │                            ├─────────►│ Mongo DB          │
                  │               │                            │          └───────────────────┘
                  │               │                            │
                  │               │                            │
                  │               └────────────────────────────┘
                  │
                  │
                  │
                  │
                  │               ┌────────────────────────────┐          ┌───────────────────┐
                  │ HTTP          │                            ├─────────►│ Redis             │
                  └──────────────►│                            │          └───────────────────┘
                                  │                            │
                                  │                            │
                                  │  Stock Controller          │           ┌───────────────────┐
                                  │                            ├──────────►│ Hibernate / JPA   │
                                  │                            │           └───────────────────┘
                                  │  Returns an error 30% of   │
                                  │  the time                  │
                                  └────────────────────────────┘
```
