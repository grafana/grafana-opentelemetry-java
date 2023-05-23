# grafana-opentelemetry-java
Grafana's distribution of the OpenTelemetry Agent for Java is based on OpenTelemetry's Extension build.

## Versions

### Java
17+

### OpenTelemetry

| Artifact                          | Version                   | Description |
|:----------------------------------|:--------------------------|-------------|
| opentelemetrySdk                  | 1.25.0                    |             |
| opentelemetryJavaagent            | 1.26.0-SNAPSHOT           |             |
| opentelemetryJavaagentAlpha       | 1.26.0-alpha-SNAPSHOT     |             |

## Build

```
./gradlew clean
```

```
./gradlew build
```

The build process will generate the following jars and place them under `build/libs`.

* grafana-opentelemetry-java-1.0-all.jar
* grafana-opentelemetry-java-1.0.jar
* grafana-opentelemetry-javaagent.jar

The `grafana-opentelemetry-javaagent.jar` also contains the `grafana-opentelemetry-java-1.0-all.jar` which contains
all custom extension and instrumentation modules.

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

## Known Issues

The tests occasionally fail due to TestContainers not starting in time.  Please rerun the build for now, until
a new wait strategy can be determined.