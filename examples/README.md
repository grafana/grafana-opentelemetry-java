# Overview

The example applications tests various instrumentation libraries.

The goal is to make it very easy to check if a certain combination of libraries and frameworks is observable
using the [Java Agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation),
e.g. Java 8 with [IBM Message Queue](https://developer.ibm.com/tutorials/mq-jms-application-development-with-spring-boot/).

# Running the examples

Each example project is run in the same way - so these instructions apply to all of them.

## Setup

### Java

Use https://asdf-vm.com/ to switch jdk versions - or just look at `.tool-versions` and select the JDK manually
before running any of the examples.

### Grafana Agent

- Go to Grafana Home page
- Click on "Connect data"
- search for "OpenTelemetry (OTLP)"
- follow the instructions there

If you're running on Linux, the script will also start the grafana agent if it is not running already.

## Starting the example application

Use `./run.sh` to start each example application.

### Debugging the example application

- Use `./run.sh --attachDebugger` to attach a debugger to https://github.com/open-telemetry/opentelemetry-java-instrumentation
  as explained in [debugging](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/contributing/debugging.md).
- Use `./run.sh --debugInstrumentations` to output the instrumentations that are loaded by the java agent.
- Use `./run.sh --debugLogging` to output all telemetry data to the console.
- Use `./run.sh --includeAllInstrumentations` to enable all instrumentations.

These options can be combined.

## Variations

### Log4j

If you want to test log4j, change the following in "build.gradle":
- add `implementation "org.springframework.boot:spring-boot-starter-log4j2"` in the dependencies section
- add ```configurations {
  all*.exclude module: 'spring-boot-starter-logging'
} ```


# Results

## Libraries used

### Spring Web

This is a traditional Spring Boot application setup.

Note that Spring Boot manages other versions (e.g. for Kafka), so those are not mentioned explicitly.
Versions not mentioned are the same as the column to the left.

| Library                   | Java 17 [^1] | Java 8 [^2] | Log4j [^2] |
|---------------------------|--------------|-------------|------------|
| Java                      | 19 [^3]      | 8 [^4]      |            |
| Java Agent                | 1.27.0       |             |            |
| Spring Boot (starter-web) | 3.1.0        | 2.7.12      |            |
| Jedis (Redis)             | 4.4.1        |             |            |
| Logback                   | 1.4.7        | 1.2.12      | not used   |
| Log4j                     | not used     | not used    | 2.7.12     |

[^1]: Demo project spring-boot-web-3.1
[^2]: Demo project spring-boot-web-2.7
[^3]: OpenJDK Runtime Environment Temurin-17.0.7+7 (build 17.0.7+7)
[^4]: OpenJDK Runtime Environment (Temurin)(build 1.8.0_372-b07)

### Spring Webflux

| Library               | WebFlux [^5] |
|-----------------------|--------------|
| Java                  | 8 [^4]       |
| Java Agent            | 1.27.0       |
| Spring Boot (webflux) | 2.7.12       |
| Jedis (Redis)         | 4.4.1        |
| Logback               | 1.2.12       |

[^5]: Demo project spring-boot-webflux-2.7

## Produced Telemetry data

If a certain feature (e.g. traces for Jedis client) only worked in a specific setup (e.g. Webflux),
it's noted separately in the section (e.g. [Jedis client span](#jedis-client-span).)

| Framework       | Traces | Metrics                   |
|-----------------|--------|---------------------------|
| JVM Overview    | N/A    | ☑️                        |
| Web Server      | ☑️     | ☑️                        |
| RestTemplate    | ☑️     | ☑️                        |
| Kafka Client    | ☑️     | ☑️                        |
| MongoDB client  | ☑️     | ☑️                        |
| Jedis client    | ☑️     | ☑️ (requires manual work) |
| Hibernate / JPA | ☑️     | ☑️                        |
| Active MQ / JMS | ☑️     | ❌                         |


### Traces

Full trace for a request to the Cart Controller:

![](doc/trace.png)

For a reactive spring application

![](doc/trace-reactive.png)

Notes:
- the nesting level in the reactive trace is "too flat" - not investigated why
- the server span "GET /controller" is missing - not investigated why

#### Web Server span

Name: GET /cart

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.method</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"GET"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.response_content_length</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">29</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.route</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"/cart"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.scheme</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.status_code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">200</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.target</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"/cart"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.host.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"localhost"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.host.port</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">8080</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.protocol.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.protocol.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.sock.host.addr</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"127.0.0.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.sock.peer.addr</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"127.0.0.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.sock.peer.port</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">57108</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.tomcat-10.0"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"server"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">56</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-7"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">user_agent.original</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"curl/7.81.0"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### Spring Web MVC span

- Shows the Java method name of the controller

Name: `CartController.getCart`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.spring-webmvc-6.0"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"internal"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">56</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-7"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### Kafka client span

Name: `topic1 send`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.destination.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"topic"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.destination.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"topic1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.kafka.client_id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"producer-1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.kafka.destination.partition</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">2</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.kafka.message.offset</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">6</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.system</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"kafka"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.kafka-clients-0.11"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"producer"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">56</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-7"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### RestTemplate client span

Name: `GET`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.method</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"GET"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.response_content_length</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">4</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.status_code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">200</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">http.url</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"<a href="http://localhost:8080/customer">http://localhost:8080/customer</a>"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.peer.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"localhost"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.peer.port</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">8080</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.protocol.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.protocol.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.http-url-connection"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"client"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">56</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-7"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### Spring Data MongoDB internal span

Name: `CustomerMongoRepository.findByFirstName`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">code.function</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"findByFirstName"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">code.namespace</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"com.grafana.springbootdemo.CustomerMongoRepository"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.spring-data-1.8"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"internal"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">51</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-2"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### MongoDB client span

Name: `find test.customer`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.connection_string</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"mongodb://localhost:27017"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.mongodb.collection</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"customer"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"test"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.operation</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"find"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.statement</td><td><div class="css-7kp13n"><div class="json-markup">{
    <span class="json-markup-key">"find":</span> <span class="json-markup-string">"customer"</span>,
    <span class="json-markup-key">"filter":</span> {
        <span class="json-markup-key">"firstName":</span> <span class="json-markup-string">"?"</span>
    },
    <span class="json-markup-key">"limit":</span> <span class="json-markup-string">"?"</span>,
    <span class="json-markup-key">"$db":</span> <span class="json-markup-string">"?"</span>,
    <span class="json-markup-key">"lsid":</span> {
        <span class="json-markup-key">"id":</span> <span class="json-markup-string">"?"</span>
    }
}</div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.system</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"mongodb"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.peer.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"localhost"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.peer.port</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">27017</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.mongo-3.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"client"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">51</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-2"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### Jedis client span

Name: `SADD`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.operation</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"SADD"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.statement</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"SADD planets ?"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.system</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"redis"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.sock.peer.addr</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"127.0.0.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.sock.peer.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"127.0.0.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">net.sock.peer.port</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">6379</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.jedis-4.0"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"client"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">52</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-3"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### Spring Data JPA internal span

Name: `ProductJpaRepository.findById`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">code.function</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"findById"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">code.namespace</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"com.grafana.springbootdemo.ProductJpaRepository"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.spring-data-1.8"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"internal"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">52</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-3"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### Hibernate internal span

Name: `Session.find com.grafana.springbootdemo.Product`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.hibernate-6.0"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"internal"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">52</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-3"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### JDBC client span

Name: `SELECT 7a4ae095-d193-45c6-bcb0-baa64fd4241e.product`

Attributes:

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.connection_string</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"h2:mem:"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"7a4ae095-d193-45c6-bcb0-baa64fd4241e"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.operation</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"SELECT"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.sql.table</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"product"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.statement</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"select p1_0.id,p1_0.name,p1_0.picture_url,p1_0.price from product p1_0 where p1_0.id=?"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.system</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"h2"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">db.user</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"sa"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.jdbc"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"client"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">52</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"http-nio-8080-exec-3"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

#### JMS

Name: `jms_destination publish`

<table class="css-1ago99h"><tbody class="css-14g0w27-body"><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.destination.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"queue"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.destination.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"jms_destination"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.message.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"ID:nevla-33549-1686830860119-4:1:1:1:5"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">messaging.system</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"jms"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"io.opentelemetry.jms-1.1"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">otel.library.version</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"1.26.0-alpha"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">span.kind</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"producer"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">status.code</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">0</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.id</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-number">56</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr><tr class="css-10clzph-row"><td class="css-mntbtq-keyColumn" data-testid="KeyValueTable--keyColumn">thread.name</td><td><div class="css-7kp13n"><div class="json-markup"><span class="json-markup-string">"reactor-http-epoll-3"</span></div></div></td><td class="css-8fecs8-copyColumn"></td></tr></tbody></table>

### Logging

- using the [Java Log pattern](https://grafana.com/docs/opentelemetry/visualization/loki-data/#common-framework-formats) is a reasonable default

`2023-06-08 10:51:31.021
INFO [nevla/main] -- com.grafana.demo.DemoApplication -- Started DemoApplication in 1.161 seconds (process running for 1.526)`

![](doc/logs.png)

Log message attributes:

<table class="css-xdnfhu-logs-row-details-table"><tbody><tr><td colspan="100" class="css-1m3a7rd-logs-row-details__heading" aria-label="Fields">Fields</td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">attributes_thread_id</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">1<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">attributes_thread_name</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">main<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">body</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">Tomcat started on port(s): 8080 (http) with context path ''<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">exporter</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">OTLP<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">instance</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">nevla<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">instrumentation_scope_name</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">org.springframework.boot.web.embedded.tomcat.TomcatWebServer<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">job</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">demo-app<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">level</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">INFO<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_host_arch</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">amd64<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_host_name</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">nevla<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_os_description</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">Linux 5.19.0-42-generic<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_os_type</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">linux<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_process_command_line</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">/home/foo/.asdf/installs/java/temurin-19.0.2+7/bin/java -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant com.grafana.demo.DemoApplication<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_process_executable_path</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">/home/foo/.asdf/installs/java/temurin-19.0.2+7/bin/java<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_process_pid</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">866314<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_process_runtime_description</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">Eclipse Adoptium OpenJDK 64-Bit Server VM 19.0.2+7<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_process_runtime_name</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">OpenJDK Runtime Environment<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_process_runtime_version</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">19.0.2+7<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_service_instance_id</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">nevla<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_service_name</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">demo-app<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_telemetry_sdk_language</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">java<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_telemetry_sdk_name</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">opentelemetry<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">resources_telemetry_sdk_version</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">1.25.0<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr><tr class="css-gi55n9-logs-row-details__row"><td class="css-1996m6s-logs-row-details__icon"><div class="css-14tlop3"></div></td><td class="css-1d24gbv-logs-row-details__label">severity</td><td class="css-1lm1wit-wordBreakAll-wrapLine"><div class="css-18efnml">INFO<div class="show-on-hover css-6wf0q6"></div><div class="css-nq2ff"></div></div></td></tr></tbody></table>

### Metrics

#### JVM overview metrics

Use https://grafana.com/grafana/dashboards/18812-jvm-overview-opentelemetry/ or the Application Observability app

#### Web Server metrics

Use Application Observability app

or manually:

- Duration: `(sum by (instance)(rate(http_server_duration_sum{job=~"$job", instance=~"$instance"}[$__rate_interval]))) /
on (instance)
(sum by (instance)(rate(http_server_duration_count{job=~"$job", instance=~"$instance"}[$__rate_interval])))
`
- Errors: `(sum by (instance)(rate(http_server_duration_count{job=~"$job", instance=~"$instance", http_status_code="500"}[$__rate_interval]))) / on (instance) (sum by (instance)(rate(http_server_duration_count{job=~"$job", instance=~"$instance"}[$__rate_interval])))`

- Rate: `sum by (instance) (rate(http_server_duration_count{job=~"$job", instance=~"$instance"}[$__rate_interval]))`

#### JDBC / HikariCP

A very useful set of metrics is the current and max. number of connections to detect a draining connection pool

- max: `db_client_connections_max{pool_name="HikariPool-1"}`
- current: `db_client_connections_usage{pool_name="HikariPool-1", state="used"}`

For reactive, `r2dbc_pool_acquired{}` seems like a good metric to monitor pool usage.

#### Kafka client metrics

There are [many metrics](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/instrumentation/kafka/kafka-clients/kafka-clients-2.6/library/README.md).
Not sure what the best practice is to get a good overview, maybe error rate: `sum(rate(kafka_producer_record_error_total[$__rate_interval]))`

#### RestTemplate client

Same metrics as for [http server](#web-server-metrics), just replace `server` by `client`,
e.g. for rate: `sum by (instance) (rate(http_client_duration_count{}[$__rate_interval]))`

#### MongoDB client

`mongodb_driver_pool_size{}` seems like a good metric.
(Note that this is a micrometer metric).

#### Jedis client

JMX provides good connection pool metrics (e.g. `NumActive`) - and JMX metrics can be turned into
OpenTelemetry metrics as described [here](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/3d0971b318d523022ea66555cb02f2c5e9607bd2/instrumentation/jmx-metrics/javaagent/README.md#configuration-files).

![](doc/jmx.png)

#### JMS

- ActiveMQ: not available
- RabbitMQ: should be available in micrometer - not tested
