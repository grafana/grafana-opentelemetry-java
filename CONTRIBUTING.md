# Contributing

This project is a javaagent distribution of the OpenTelemetry Java instrumentation agent.
It is modeled after the [OpenTelemetry Java instrumentation agent distro template](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/examples/distro/README.md).

## Build

```sh
./gradlew clean
```

```sh
./gradlew build
```

The build process will generate the following jars and place them under `build/libs`.

* grafana-opentelemetry-java-1.0-all.jar
* grafana-opentelemetry-java-1.0.jar
* grafana-opentelemetry-javaagent.jar

The `grafana-opentelemetry-javaagent.jar` also contains the `grafana-opentelemetry-java-1.0-all.jar` which contains
all custom extension and instrumentation modules.
