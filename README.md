# grafana-opentelemetry-java
Grafana's distribution of the OpenTelemetry Agent for Java is based on OpenTelemetry's Extension build.

## Versions

### Java
8+

### OpenTelemetry

| Artifact                          | Version                   | Description |
|:----------------------------------|:--------------------------|-------------|
| opentelemetrySdk                  | 1.25.0                    |             |
| opentelemetryJavaagent            | 1.26.0-SNAPSHOT           |             |
| opentelemetryJavaagentAlpha       | 1.26.0-alpha-SNAPSHOT     |             |


## Implement
These instructions assume that you are running the Grafana Agent and that the HTTP server and/or gRPC server
is running on the appropriate default port.  If this is not the case, you will need to set `OTEL_EXPORTER_OTLP_ENDPOINT` with 
the correct endpoint.

To attach the newly built javaagent to your application, run the following commands.

```
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4318"
export OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
java -javaagent:grafana-opentelemetry-javaagent.jar -jar <PATH_TO_JAVA_APP_JAR>
```
or

```
export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
export OTEL_EXPORTER_OTLP_PROTOCOL="gRPC"
java -javaagent:grafana-opentelemetry-javaagent.jar -jar <PATH_TO_JAVA_APP_JAR>
```

## Enable Exporter Logging

To assist with development and troubleshooting, you may want to enable `logging` exporters.  You can do so by adding
logging to a given otel exporter property/enviroment variable.  Below is an example
```
export OTEL_METRICS_EXPORTER="otlp,logging"
export OTEL_TRACES_EXPORTER="otlp,logging"
export OTEL_LOGS_EXPORTER="otlp,logging"
```

Or you can use the following Grafana property/environment variable to manage `logging` exporters.
```
export GRAFANA_OTEL_LOGGING_EXPORTER_ENABLED="metrics,logs,traces"
```
The above would enable `logging` for all signal types (Note that order/case do not matter).  If you only wish to enable logging for specific 
signals, simply include those of interest in the list.  The following would only enable logging for metrics data.

```
export GRAFANA_OTEL_LOGGING_EXPORTER_ENABLED="metrics"
```



## Known Issues

The tests occasionally fail due to TestContainers not starting in time.  Please rerun the build for now, until
a new wait strategy can be determined.

## Supported Libraries

This is a dummy text here, just to test that the list of supported libraries can be parsed from this file at build time.
                    
Instrumentation Modules
                                                                                                              
| ID                                  | Name                                                                                                                                                                                 |
|-------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| opentelemetry-extension-annotations | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0b2de635f24fe0f) |
| opentelemetry-api                   | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0b2de635f24fe0f) |
| tomcat                              | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0b2de635f24fe0f) |
| spring-web                          | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0b2de635f24fe0f) |
| spring-webmvc                       | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0b2de635f24fe0f) |
| spring-data                         | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0b2de635f24fe0f) |
| jms                                 | [todo (the link is internal once merged)](https://github.com/grafana/grafana-opentelemetry-java/pull/17/files#diff-912c0488fe6c6df14ae6491c64e3a302553cfc2f07ce83f9b0b2de635f24fe0f) |
