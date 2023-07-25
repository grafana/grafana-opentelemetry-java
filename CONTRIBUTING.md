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

The build process will generate `agent/build/libs/grafana-opentelemetry-java.jar`, which contains the upstream
javaagent.jar as well as our custom extension.

## Known Issues

The tests occasionally fail due to TestContainers not starting in time.  Please rerun the build for now, until
a new wait strategy can be determined.
