<!-- markdownlint-disable -->
<p>
  <img src="https://upload.wikimedia.org/wikipedia/commons/3/3b/Grafana_icon.svg" alt="Grafana logo" height="70"/ >
  <img src="https://opentelemetry.io/img/logos/opentelemetry-logo-nav.png" alt="OpenTelemetry logo" width="70"/ >
</p>
<!-- markdownlint-enable -->

# Grafana OpenTelemetry Distribution for Java

[![Build](https://github.com/grafana/grafana-opentelemetry-java/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/grafana/grafana-opentelemetry-java/actions/workflows/build.yml)
[![Slack](https://img.shields.io/badge/join%20slack-%23app--o11y-brightgreen.svg?logo=slack)](https://grafana.slack.com/archives/C05E87XRK3J)

## About

Grafana Distribution of [OpenTelemetry Instrumentation for Java] -
optimized for [Grafana Cloud Application Observability].

![Application Observability](https://grafana.com/media/blog/otel-distro-java/application-observability-grafana-cloud-overview-go.png)

This project provides a Java agent JAR that can be attached to any Java 8+ application and dynamically
injects bytecode to capture telemetry from a number of popular libraries and frameworks.

Why use this distribution instead of [OpenTelemetry Instrumentation for Java] (upstream) directly?

- **Easy to get started**: This distribution is optimized for [Grafana Cloud Application Observability] -
  you can get started with just a few environment variables.
- **Fully Compatible**: This distribution is fully compatible with upstream -
  you can use all configuration options of upstream.
- **Optimized for Application Observability and Grafana Agent**: No need to tweak any configuration settings
  if you use the latest version of the Grafana Agent.
- **Fast Bug Fixes**: We can fix bugs faster without waiting for the next release of
  upstream.
- **Cost Optimized**: You can save costs by sending only the metrics that are actually used by the dashboards in
  Application Observability ([opt-in](#data-saver)).
  
> **Open Source Friendly**:
>
> - You can use [OpenTelemetry Instrumentation for Java] directly for [Grafana Cloud Application Observability] -
>   this distribution is just a convenience wrapper.
>   You can find more information how to send telemetry data to Grafana Cloud Databases
>   [here](https://grafana.com/docs/opentelemetry/collector/send-otlp-to-grafana-cloud-databases/).
> - You can use this distribution for any OpenTelemetry use case, not just Grafana Cloud.
> - You can migrate from this distribution to OpenTelemetry Instrumentation for Java as explained
>   [here](#migrating-to-opentelemetry-instrumentation-for-java).

## Community

To engage with the Grafana Cloud Application Observability community:

- Chat with us on our community Slack channel. To invite yourself to the
  Grafana Slack, visit [https://slack.grafana.com/](https://slack.grafana.com)
  and join the [#application-observability](https://grafana.slack.com/archives/C05E87XRK3J)
channel.
- Ask questions on the [Discussions page](https://github.com/grafana/grafana-opentelemetry-java/discussions).
- [File an issue](https://github.com/grafana/grafana-opentelemetry-java/issues/new)
  for bugs, enhancements, and feature suggestions.

## Compatibility

- Java 8+
- Spring Boot 2.7+
  - Older Spring Boot versions also mostly work, but this is not tested explicitly
  - Jetty is supported for Spring Boot 3.2+, because it requires Jetty 12, which is not supported
- We regularly update to the latest version of [OpenTelemetry Instrumentation for Java] - you can find the current
  version [here](https://github.com/grafana/grafana-opentelemetry-java/blob/main/build.gradle#L6)
- [Tested Libraries](#tested-instrumentations)

## Getting Started

For production, we recommend to use [Grafana Agent], because Application Observability is optimized for it
(e.g. feature toggles).
[Opentelemetry Collector] is also supported for production use cases.

For an easier local development setup, send directly to Grafana Cloud OTLP Gateway.

### Quickstart

**Note**: The [Grafana Cloud OTLP Gateway](https://grafana.com/docs/grafana-cloud/send-data/otlp/send-data-otlp/)
is a managed service that is available in all Grafana Cloud plans.

> 💨 **Try it out quickly with Spring PetClinic**: Follow the instructions in the [Dockerfile](Dockerfile).

#### Quickstart Step 1: Download the instrumentation agent

Download the latest release of this distribution from the
[releases page](https://github.com/grafana/grafana-opentelemetry-java/releases).

#### Quickstart Step 2: Instrument an application

1. Sign in to [Grafana Cloud](https://grafana.com), register for a Free Grafana Cloud account if required.

2. After successful login, the browser will navigate to the Grafana Cloud Portal page <https://grafana.com/profile/org>.

   A new account will most likely belong to one organization with one stack.

   If the account has access to multiple Grafana Cloud Organizations, select an organization from the
   top left **organization dropdown**.

   If the organization has access to multiple Grafana Cloud Stacks, navigate to a stack from the **left side bar**
   or the main **Stacks** list.

3. With a stack selected, or in the single stack scenario, below **Manage your Grafana Cloud Stack**,
   click **Configure** in the **OpenTelemetry** section:

   ![otel tile](https://grafana.com/media/docs/grafana-cloud/application-observability/opentelemetry-tile.png)

4. In the **Password / API Token** section, click on **Generate now** to create a new API token:
   - Give the API token a name, for example `otel-java`
   - Click on **Create token**
   - Click on **Close** without copying the token
   - Now the environment variables section is populated with all the necessary information to send telemetry data
     to Grafana Cloud
   - Click on **Copy to Clipboard** to copy the environment variables to the clipboard

   ![otel env vars](https://grafana.com/media/docs/grafana-cloud/application-observability/opentelemetry-env-vars.png)

5. Come up with a **Service Name** to identify the service, for example `cart`, and copy it into the shell command
   below. Use the `service.namespace` to group multiple services together.
6. Optional: add resource attributes to the shell command below:
   - **deployment.environment**: Name of the deployment environment, for example `staging` or `production`
   - **service.namespace**: A namespace to group similar services, for example using `service.namespace=shop` for a
     `cart` and `fraud-detection` service would create `shop/cart` and `shop/fraud-detection` in Grafana Cloud
     Application Observability with filtering capabilities for easier management
   - **service.version**: The application version, to see if a new version has introduced a bug
   - **service.instance.id**: The unique instance, for example the Pod name (a UUID is generated by default)

Add the `-javaagent` option to the java command with the following environment variables
(example [Dockerfile](Dockerfile)):[^1].

```shell
<Paste the environment variables from the previous section>
export OTEL_SERVICE_NAME=<Service Name>
export OTEL_RESOURCE_ATTRIBUTES=deployment.environment=<Environment>,service.namespace=<Namespace>,service.version=<Version>
java -javaagent:path/to/grafana-opentelemetry-java.jar -jar myapp.jar
```

Finally, make some requests to the service to validate data is sent to Grafana Cloud.
It might take up to five minutes for data to appear.

#### Quickstart Step 3: Observe the Service in Application Observability

In Grafana, replace the path of the URL with `/a/grafana-app-observability-app/services` or:

1. Click on the menu icon in the top left corner
2. Open the _Observability_ menu
3. Click on _Application_

**Important**: refer to the [troubleshooting guide](#troubleshooting) if there is no data in Application Observability.

### Grafana Agent

The Grafana Agent is a single binary that can be deployed as a sidecar or daemonset in Kubernetes,
or as a service in your network. It provides an endpoint where the application can send its telemetry data to.
The telemetry data is then forwarded to Grafana Cloud.

> 💨 Skip this section and let the [OpenTelemetry Integration](https://grafana.com/docs/grafana-cloud/data-configuration/integrations/integration-reference/integration-opentelemetry/)
> create everything for you.

#### Grafana Agent Step 1: Download the instrumentation agent

Download the latest release of this distribution from the
[releases page](https://github.com/grafana/grafana-opentelemetry-java/releases).

#### Grafana Agent Step 2: Instrument an application

1. If the Grafana Agent is not running locally or doesn't use the default grpc endpoint,
   adjust OTEL_EXPORTER_OTLP_ENDPOINT or OTEL_EXPORTER_OTLP_PROTOCOL (to `http/protobuf`).
2. Choose a **Service Name** to identify the service.
3. Optionally, add attributes to filter data:
   - **deployment.environment**: Name of the deployment environment (`staging` or `production`)
   - **service.namespace**: A namespace to group similar services
     (e.g. `shop` would create `shop/cart` in Application Observability)
   - **service.version**: The application version, to see if a new version has introduced a bug
   - **service.instance.id**: The unique instance, for example the Pod name (a UUID is generated by default)

Add the `-javaagent` option to the java command with the following environment variables
(example [Dockerfile](Dockerfile.production)):[^1].

```shell
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_SERVICE_NAME=<Service Name>
export OTEL_RESOURCE_ATTRIBUTES=deployment.environment=<Environment>,service.namespace=<Namespace>,service.version=<Version>
java -javaagent:path/to/grafana-opentelemetry-java.jar -jar myapp.jar
```

The application will send data to the Grafana Agent. Please follow the
[Grafana Agent configuration for OpenTelemetry](https://grafana.com/docs/opentelemetry/instrumentation/configuration/grafana-agent/)
guide.

Finally, make some requests to the service to validate data is sent to Grafana Cloud.
It might take up to five minutes for data to appear.

#### Grafana Agent Step 3: Observe the Service in Application Observability

In Grafana, replace the path of the URL with `/a/grafana-app-observability-app/services` or:

1. Click on the menu icon in the top left corner
2. Open the _Observability_ menu
3. Click on _Application_

**Important**: refer to the [troubleshooting guide](#troubleshooting) if there is no data in Application Observability.

### OpenTelemetry Collector

> 💨 **Try it out quickly with Spring PetClinic**: Follow the instructions in the [Dockerfile](Dockerfile.production).

#### OpenTelemetry Collector Step 1: Download the instrumentation agent

Download the latest release of this distribution from the
[releases page](https://github.com/grafana/grafana-opentelemetry-java/releases).

#### OpenTelemetry Collector Step 2: Instrument an application

1. If the OpenTelemetry Collector is not running locally or doesn't use the default grpc endpoint,
   adjust OTEL_EXPORTER_OTLP_ENDPOINT or OTEL_EXPORTER_OTLP_PROTOCOL (to `http/protobuf`).
2. Choose a **Service Name** to identify the service.
3. Optionally, add attributes to filter data:
   - **deployment.environment**: Name of the deployment environment (`staging` or `production`)
   - **service.namespace**: A namespace to group similar services
     (e.g. `shop` would create `shop/cart` in Application Observability)
   - **service.version**: The application version, to see if a new version has introduced a bug
   - **service.instance.id**: The unique instance, for example the Pod name (a UUID is generated by default)

Add the `-javaagent` option to the java command with the following environment variables
(example [Dockerfile](Dockerfile.production)):[^1].

```shell
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
export OTEL_EXPORTER_OTLP_PROTOCOL=grpc
export OTEL_SERVICE_NAME=<Service Name>
export OTEL_RESOURCE_ATTRIBUTES=deployment.environment=<Environment>,service.namespace=<Namespace>,service.version=<Version>
java -javaagent:path/to/grafana-opentelemetry-java.jar -jar myapp.jar
```

The application will send data to the OpenTelemetry Collector. Please follow the
[OpenTelemetry Collector configuration for OpenTelemetry](https://grafana.com/docs/opentelemetry/collector/send-otlp-to-grafana-cloud-databases/)
guide.

Finally, make some requests to the service to validate data is sent to Grafana Cloud.
It might take up to five minutes for data to appear.

#### OpenTelemetry Collector Step 3: Observe the Service in Application Observability

In Grafana, replace the path of the URL with `/a/grafana-app-observability-app/services` or:

1. Click on the menu icon in the top left corner
2. Open the _Observability_ menu
3. Click on _Application_

**Important**: refer to the [troubleshooting guide](#troubleshooting) if there is no data in Application Observability.

## Troubleshooting

If you don't see any data in Application Observability, these are the most common causes:

### No Traffic

Make a few requests to the service to make sure it sends data to Grafana Cloud.

### Be Patient

Even after you've made a few requests, it can take up to 5 minutes
until the data is visible in Application Observability.

### Look for errors

Look for errors - either on the console or in docker or Kubernetes logs
(using Application Observability logs doesn't make sense in this case).

If there are errors sending telemetry data, one of the parameters is usually wrong.
A 5xx response code means that there's something wrong with the [Grafana Cloud OTLP Gateway].

### Log all sent telemetry data

If there are no errors in the logs, make sure that the application is actually sending data all using
[debug logging](#enable-otlp-debug-logging).
If the application is not sending data, the java agent was probably not loaded - look for the `-javaagent` command line
parameter.

### Log telemetry data in Grafana Agent or OpenTelemetry Collector

If the application is sending data to a [Grafana Agent] or [OpenTelemetry Collector] instead of
[Grafana Cloud OTLP Gateway], make sure that there is no error forwarding the telemetry data.

### Enable debug logging in javaagent

To turn on the javaagent's internal debug logging:

```shell
export OTEL_JAVAAGENT_DEBUG=true
```

**Note:** These logs are extremely verbose. Enable debug logging only when needed.
Debug logging negatively impacts the performance of your application.

### Disable javaagent

If your service causes errors, and you want to check if it's because of a bug in the java agent,
you can disable the java completely:

```shell
export OTEL_JAVAAGENT_ENABLED=false
```

## Reference

- In addition to the configuration explained above, you can use all system properties or environment variables from the
  [SDK auto-configuration](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure),
  which will take precedence.
- All exporters are set to `otlp` by default (even the logs exporter).

### Enable OTLP Debug Logging

OTLP debug logging can be configured via environment variables:

| Configuration                        | Result                                                |
|--------------------------------------|-------------------------------------------------------|
| `OTEL_SPAN_EXPORTER=otlp,logging`    | Log all spans. This will also send spans to Loki.     |
| `OTEL_METRICS_EXPORTER=otlp,logging` | Log all metrics. This will also send metrics to Loki. |
| `OTEL_LOGS_EXPORTER=otlp,logging`    | Log all logs (again, just to see they are sent).      |

**Note that this is going to produce a lot of log output, so it should be disabled again as soon as possible.**

This will produce log output like this:

Spans - look for `LoggingSpanExporter`:

```shell
INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'WebController.withSpan' : 2337335133908c9ce6e0dfc7bda74d7c 8bfef4eaac83e8cb INTERNAL [tracer: io.opentelemetry.opentelemetry-extension-annotations-1.0:1.32.0-alpha] AttributesMap{data={thread.id=32, code.namespace=io.opentelemetry.smoketest.springboot.controller.WebController, code.function=withSpan, thread.name=http-nio-8080-exec-1}, capacity=128, totalAddedValues=4}
```

Metrics - look for `LoggingMetricExporter`:

```shell
INFO io.opentelemetry.exporter.logging.LoggingMetricExporter - metric: ImmutableMetricData{resource=Resource{schemaUrl=https://opentelemetry.io/schemas/1.21.0, attributes={container.id="048b9982e0b98cdc5579334bb1decc157ed1ebc23f391ebe306898898ec32fa4", host.arch="amd64", host.name="048b9982e0b9", os.description="Linux 6.2.0-39-generic", os.type="linux", process.command_line="/usr/lib/jvm/jdk-8u312-bellsoft-x86_64/jre/bin/java -javaagent:/opentelemetry-javaagent.jar -Dgrafana.otel.use-tested-instrumentations=true io.opentelemetry.smoketest.springboot.SpringbootApplication", process.executable.path="/usr/lib/jvm/jdk-8u312-bellsoft-x86_64/jre/bin/java", process.pid=1, process.runtime.description="BellSoft OpenJDK 64-Bit Server VM 25.312-b07", process.runtime.name="OpenJDK Runtime Environment", process.runtime.version="1.8.0_312-b07", service.instance.id="8231ca95-e9aa-474a-bd98-88349a9942ad", service.name="unknown_service:java", telemetry.auto.version="1.32.0", telemetry.distro.name="grafana-opentelemetry-java", telemetry.distro.version="0.32.0-beta.1", telemetry.sdk.language="java", telemetry.sdk.name="opentelemetry", telemetry.sdk.version="1.32.0"}}, instrumentationScopeInfo=InstrumentationScopeInfo{name=io.opentelemetry.runtime-telemetry-java8, version=1.32.0-alpha, schemaUrl=null, attributes={}}, name=jvm.cpu.count, description=Number of processors available to the Java virtual machine., unit={cpu}, type=LONG_SUM, data=ImmutableSumData{points=[ImmutableLongPointData{startEpochNanos=1704964347622000000, epochNanos=1704964351627000000, attributes={}, value=12, exemplars=[]}], monotonic=false, aggregationTemporality=CUMULATIVE}}
```

Logs - look for `[scopeInfo:` and duplicated log body
(the second line is just for reference to see that is also contains `HTTP request received`):

```shell
10:12:34.031 [docker-java-stream-636643381] INFO  c.g.extensions.smoketest.SmokeTest - STDOUT: 2024-01-11T09:12:34.03Z INFO 'HTTP request received' : 2337335133908c9ce6e0dfc7bda74d7c 50d689015fd0a33c [scopeInfo: io.opentelemetry.smoketest.springboot.controller.WebController:] {thread.id=32, thread.name="http-nio-8080-exec-1"}
10:12:34.031 [docker-java-stream-636643381] INFO  c.g.extensions.smoketest.SmokeTest - STDOUT: INFO  [http-nio-8080-exec-1] io.opentelemetry.smoketest.springboot.controller.WebController: HTTP request received trace_id=
```

### Tested Instrumentations

This project provides end-to-end tests for a number of libraries. The tests are located in the `examples` folder and
are run by [oats](https://github.com/grafana/oats/). They cover the integration into the Grafana LTGM stack.

You can run the Grafana distribution in a mode that includes all instrumentation modules that are covered by the tests,
no more, no less.

```shell
export GRAFANA_OTEL_USE_TESTED_INSTRUMENTATIONS=true
```

These are the tested instrumentations:

| ID                                        | Name                                                                                                                                              |
|-------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| opentelemetry-extension-annotations       | [@WithSpan annotation](./examples/manual/README.md)                                                                                               |
| opentelemetry-instrumentation-annotations | [@WithSpan annotation](./examples/manual/README.md)                                                                                               |
| opentelemetry-api                         | [Manual instrumentation](./examples/manual/README.md)                                                                                             |
| tomcat                                    | Non-reactive spring boot default web server, e.g. [JDBC](./examples/jdbc/spring-boot-non-reactive-3)                                              |
| jetty                                     | [Jetty Web Server](./examples/jetty/README.md)                                                                                                    |
| netty                                     | Reactive spring boot web server, e.g. in [JDBC](./examples/jdbc/spring-boot-reactive-3)                                                           |
| undertow                                  | [Undertow Web Server](./examples/undertow/README.md)                                                                                              |
| spring-web                                | Non-reactive spring boot, e.g. in [JDBC](./examples/jdbc/spring-boot-non-reactive-3)                                                              |
| spring-webmvc                             | Non-reactive spring boot, e.g. in [JDBC](./examples/jdbc/spring-boot-non-reactive-3)                                                              |
| spring-webflux                            | Reactive spring boot, e.g. in [JDBC](./examples/jdbc/spring-boot-reactive-3)                                                                      |
| reactor                                   | Reactive spring boot, e.g. in [JDBC](./examples/jdbc/spring-boot-reactive-3)                                                                      |
| spring-data                               | [JDBC Database Clients](./examples/jdbc/README.md)                                                                                                |
| jdbc                                      | [JDBC Database Clients](./examples/jdbc/README.md)                                                                                                |
| hikaricp                                  | [JDBC Database Clients](./examples/jdbc/README.md)                                                                                                |
| r2dbc                                     | [JDBC Database Clients](./examples/jdbc/README.md)                                                                                                |
| jms                                       | [JMS with ActiveMQ](./examples/jms/README.md)                                                                                                     |
| logback-appender                          | [Logback logs](./examples/logback/README.md)                                                                                                      |
| log4j-appender                            | [Log4j logs](./examples/log4j/README.md)                                                                                                          |
| runtime-telemetry                         | JVM Runtime Metrics - used in all examples, e.g. in [JDBC](./examples/jdbc/README.md)                                                             |
| executors                                 | Support library to synchronize thread local when using [Executors](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Executors.html) |
| micrometer                                | Micrometer is used in some examples where the JavaAgent doesn't have a metric, e.g. [MongoDB](./examples/mongodb/README.md)                       |
| kafka-clients                             | [Kafka](./examples/kafka/README.md)                                                                                                               |
| spring-kafka                              | [Kafka](./examples/kafka/README.md)                                                                                                               |
| mongo                                     | [MongoDB](./examples/mongodb/README.md)                                                                                                           |
| jedis                                     | [Redis with Jedis client](./examples/redis/README.md)                                                                                             |
| lettuce                                   | [Redis with Jedis client](./examples/redis/README.md)                                                                                             |

### Data Saver

You can reduce metrics costs by turning off all metrics that are not used by the dashboards in
Application Observability.

```shell
export GRAFANA_OTEL_APPLICATION_OBSERVABILITY_METRICS=true
```

> **Note**: If you're creating metrics manually, you can enable them by setting the meter name to `application`.

The following metrics are currently (or planned to be) used by Application Observability:

| Metric                                 | Description                                                            |
|----------------------------------------|------------------------------------------------------------------------|
| jvm.cpu.recent_utilization             | Used in the JVM tab in Application Observability                       |
| jvm.memory.used                        | Used in the JVM tab in Application Observability                       |
| jvm.memory.limit                       | Used in the JVM tab in Application Observability                       |
| jvm.gc.duration                        | Used in the JVM tab in Application Observability                       |
| jvm.class.count                        | Used in the JVM tab in Application Observability                       |
| jvm.thread.count                       | Used in the JVM tab in Application Observability                       |
| db.client.connections.usage            | Used in [JDBC dashboard](https://grafana.com/grafana/dashboards/19732) |
| db.client.connections.max              | Used in [JDBC dashboard](https://grafana.com/grafana/dashboards/19732) |
| db.client.connections.pending_requests | Used in [JDBC dashboard](https://grafana.com/grafana/dashboards/19732) |
| r2dbc.pool.acquired                    | Used by [reactive Database example](examples/jdbc/README.md)           |
| r2dbc.pool.max.allocated               | Used by [reactive Database example](examples/jdbc/README.md)           |
| r2dbc.pool.pending                     | Used by [reactive Database example](examples/jdbc/README.md)           |
| kafka.producer.record_error_total      | Used by [Kafka example](examples/kafka/README.md)                      |
| mongodb.driver.pool.waitqueuesize      | Used by [MongoDB example](examples/mongodb/README.md)                  |
| mongodb.driver.pool.checkedout         | Used by [MongoDB example](examples/mongodb/README.md)                  |

### Migrating to OpenTelemetry Instrumentation for Java

Follow these steps if you want to migrate from this distribution to the upstream project
OpenTelemetry Instrumentation for Java:

- Replace all environment variables or system properties with the "grafana" prefix as explained
  [here](https://grafana.com/docs/grafana-cloud/send-data/otlp/send-data-otlp/#push-directly-from-applications-using-the-opentelemetry-sdks).
- Add the `service.instance.id` to the `OTEL_RESOURCE_ATTRIBUTES`, e.g. `OTEL_RESOURCE_ATTRIBUTES=service.instance.id=<pod123>,deployment.environment=...`
  where `<pod123>` it the name of the Kubernetes pod or some other unique identifier within the service
  (future versions of OpenTelemetry Instrumentation for Java might include this feature).
- If you use [Data Saver](#data-saver), you can filter the metrics in the OpenTelemetry Collector instead
  ([docs](https://opentelemetry.io/docs/collector/transforming-telemetry/#basic-filtering)).
  The Data Saver section lists all metrics to keep.
- The resource detectors for Kubernetes (for EKS and GKE) are not bundled in OpenTelemetry Instrumentation for Java.
  You can get the same and more resource attributes using the OpenTelemetry collector
  ([docs](https://grafana.com/docs/opentelemetry/collector/enriching-attributes-in-cloud/#adding-kubernetes-resource-attributes)).
- Add the environment variables below for the best experience with Application Observability:

```shell
export OTEL_SEMCONV_STABILITY_OPT_IN=http
export OTEL_INSTRUMENTATION_MICROMETER_BASE_TIME_UNIT=s
export OTEL_INSTRUMENTATION_LOG4J_APPENDER_EXPERIMENTAL_LOG_ATTRIBUTES=true
export OTEL_INSTRUMENTATION_LOGBACK_APPENDER_EXPERIMENTAL_LOG_ATTRIBUTES=true 
```

[OpenTelemetry Instrumentation for Java]: https://github.com/open-telemetry/opentelemetry-java-instrumentation
[Grafana Cloud Application Observability]: https://grafana.com/docs/grafana-cloud/monitor-applications/application-observability/
[Grafana Cloud OTLP Gateway]: #quickstart
[Grafana Agent]: #grafana-agent
[OpenTelemetry Collector]: #opentelemetry-collector

[^1]:
    System properties can be used instead of environment variables, for example
    `-Dotel.service.name=<OTEL_SERVICE_NAME>` instead of `export OTEL_SERVICE_NAME=<OTEL_SERVICE_NAME>`.
