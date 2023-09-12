# grafana-opentelemetry-java

## About

The Grafana distribution of the [OpenTelemetry Javaagent].

This project provides a Java agent JAR that can be attached to any Java 8+
application and dynamically injects bytecode to capture telemetry from a
number of popular libraries and frameworks.

As this is the Grafana distribution, there are some settings that make it easy to connect to Grafana Cloud or a 
Grafana OSS stack - but all configuration options of the [OpenTelemetry Javaagent] are available as well.  

## Compatibility

- Java 8+
- We regularly update to the latest version of the [OpenTelemetry Javaagent] - you can find the current version [here](https://github.com/grafana/grafana-opentelemetry-java/blob/main/build.gradle#L6)
- [Tested Libraries](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/tested-libraries.md#libraries--frameworks)

## Getting Started
                                    
### Configure your application

You can use the [Grafana Agent](#grafana-agent) or the [Grafana Cloud OTLP Gateway](#grafana-cloud-otlp-gateway) to send telemetry data to Grafana Cloud.

#### Grafana Cloud OTLP Gateway

> ⚠️ Please use the Grafana Agent configuration for production use cases.

The easiest setup is to use the Grafana Cloud OTLP Gateway, because you don't need to run any service to transport
the telemetry data to Grafana Cloud. 
The Grafana Cloud OTLP Gateway is a managed service that is available in all Grafana Cloud plans.

First, download the latest release from the [releases page](https://github.com/grafana/grafana-opentelemetry-java/releases).

If you're just getting started with Grafana Cloud, you can [sign up for permanent free plan](https://grafana.com/products/cloud/).

1. Click on "Details" button in the "Grafana" section on https://grafana.com/profile/org
2. Copy "Instance ID" and "Zone" into the java command below
3. On the left side, click on "Security" and then on "API Keys" 
4. Click on "Create API Key" (MetricsPublisher role) and copy the key into the java command below

Enable the instrumentation agent using the `-javaagent` flag to the JVM.

```shell
java -javaagent:path/to/opentelemetry-javaagent.jar \
  -Dgrafana.otlp.cloud.instance.id=<GRAFANA_INSTANCE_ID> \
  -Dgrafana.otlp.cloud.zone=<GRAFANA_ZONE> \
  -Dgrafana.otlp.cloud.api.key=<GRAFANA_CLOUD_API_KEY> \
  -Dotel.service.name=shopping-cart \
  -Dotel.resource.attributes=deployment.environment=production,service.namespace=shop,service.version=1.1,service.instance.id=shopping-cart-66b6c48dd5-hprdn \
  -jar myapp.jar
```

- Please replace `demo`, `1.1`, and `shopping-cart-66b6c48dd5-hprdn` as explained [here]({{https://grafana.com/docs/opentelemetry/instrumentation/configuration/resource-attributes/}}).  
- If the service.name is not set, the name of the jar file will be used as service name.
- If the service.instance.id is not set, it will fall back to `<k8s.pod.name>/<k8s.container.name>` (if provided) or a random UUID.
- Note that service name can also be set in `otel.resource.attributes` using the key `service_name` 
  (ex. `service_name=demo`).
- Also note that you can use [environment variables](https://grafana.com/docs/opentelemetry/instrumentation/configuration/environment-variables/) instead of system properties for all configuration options.

#### Grafana Agent

The Grafana Agent is a single binary that can be deployed as a sidecar or daemonset in Kubernetes, or as a service 
in your network. It provides an endpoint where the application can send its telemetry data to.
The telemetry data is then forwarded to Grafana Cloud or a Grafana OSS stack.

First, download the latest release from the [releases page](https://github.com/grafana/grafana-opentelemetry-java/releases).

> **Note**: If you use **Grafana Cloud**, follow the 
> [OpenTelemetry Integration](https://grafana.com/docs/grafana-cloud/data-configuration/integrations/integration-reference/integration-opentelemetry/),
> which creates a Grafana Agent configuration for you.
> Instead of using the download link for the javaagent in the integration, 
> you can use the download link from the releases page.

Enable the instrumentation agent using the `-javaagent` flag to the JVM.

```shell
java -javaagent:path/to/opentelemetry-javaagent.jar \
  -Dotel.exporter.otlp.endpoint=http://localhost:4317 \
  -Dotel.exporter.otlp.protocol=grpc \
  -Dotel.service.name=shopping-cart \
  -Dotel.resource.attributes=deployment.environment=production,service.namespace=shop,service.version=1.1,service.instance.id=shopping-cart-66b6c48dd5-hprdn \
  -jar myapp.jar
```

The application will send data to the Grafana Agent. Please follow the 
[Grafana Agent configuration for OpenTelemetry](https://grafana.com/docs/opentelemetry/instrumentation/configuration/grafana-agent/) guide.

- If the grafana agent is **not** running locally with the default gRPC endpoint (localhost:4317), then you need to
  adjust endpoint and protocol.
- Please replace `demo`, `1.1`, and `shopping-cart-66b6c48dd5-hprdn` as explained [here]({{https://grafana.com/docs/opentelemetry/instrumentation/configuration/resource-attributes/}}).  
- If the service.name is not set, the name of the jar file will be used as service name.
- If the service.instance.id is not set, it will fall back to `<k8s.pod.name>/<k8s.container.name>` (if provided) or a random UUID.
- Note that service name can also be set in `otel.resource.attributes` using the key `service_name` 
  (ex. `service_name=demo`).
- Also note that you can use [environment variables](https://grafana.com/docs/opentelemetry/instrumentation/configuration/environment-variables/) instead of system properties for all configuration options.

### Grafana Dashboard

You can use [this dashboard](https://grafana.com/grafana/dashboards/18812-jvm-overview-opentelemetry) to get
and overview about the most important JVM metrics: CPU, memory, classes, threads, and garbage collection.

<img src="docs/jvm-dashboard.png" alt="JVM Dashboard"><br/>

### Getting Help 

If anything is not working, or you have questions about the starter, we’re glad to help you on our 
[community chat](https://slack.grafana.com/) (#opentelemetry).

## Reference

- In addition to the configuration explained above, you can use all system properties or environment variables from the
  [SDK auto-configuration](https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure) -
  which will take precedence.
- All exporters are set to `otlp` by default (even the logs exporter).

### Enable Debug Logging

Log all metrics, traces, and logs that are created for debugging purposes (in addition to sending them to the backend via OTLP).

This will also send metrics and traces to Loki as an unintended side effect.

Add the following command line parameter:

```shell
-Dgrafana.otlp.debug.logging=true 
```

For more fine-grained control, you can also enable debug logging for specific signal types:

```shell
export GRAFANA_OTLP_LOGGING_EXPORTER_ENABLED="metrics,logs,traces"
```

The above would enable debug logging for all signal types (Note that order/case do not matter).  
If you only wish to enable logging for specific signals, simply include those of interest in the list.

The following would only enable logging for metrics data.

```shell
export GRAFANA_OTLP_LOGGING_EXPORTER_ENABLED="metrics"
```

### Tested Libraries

This is a dummy text here, just to test that the list of tested libraries can be parsed from this file at build time.
                    
Instrumentation Modules
                                                                                                              
| ID                                  | Name                                                                                                                                                                                 |
|-------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| opentelemetry-extension-annotations | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| opentelemetry-api                   | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| tomcat                              | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| spring-web                          | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| spring-webmvc                       | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| spring-data                         | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| jms                                 | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| logback-appender                    | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| log4j-appender                      | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| runtime-telemetry                   | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
| executors                   | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0.1de635f24fe0f) |
                                                                                                                                

[OpenTelemetry Javaagent]: https://github.com/open-telemetry/opentelemetry-java-instrumentation
