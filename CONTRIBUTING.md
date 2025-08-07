# Contributing

This project is a javaagent distribution of the OpenTelemetry Java instrumentation agent. It is modeled after the
[OpenTelemetry Java instrumentation agent distro template](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/examples/distro/README.md).

## Build

```sh
./gradlew clean
```

```sh
./gradlew build
```

The build process will generate `agent/build/libs/grafana-opentelemetry-java.jar`, which contains the upstream
javaagent.jar as well as our custom extension.

## Code formatting

- Java: `./gradlew spotlessApply`
- Markdown lint: `markdownlint -f .` (`-f` fixes simple violations, requires [markdownlint](https://github.com/DavidAnson/markdownlint#markdownlint))
- Markdown link checker: `lychee --include-fragments --max-retries 6 .`
  (requires [lychee](https://github.com/lycheeverse/lychee))

## Smoke Tests

Smoke tests test the entire javaagent distribution, including the custom extension. To run the smoke tests,
run the following command:

```sh
SMOKE_TEST_JAVA_VERSION=8 ./gradlew :smoke-tests:test
```

### Common problems

#### Instrumentation not included

Check if the test passes with `TESTCASE_INCLUDE_ALL_INSTRUMENTATIONS=true`.

If yes, check what the instrumentation scope is, and include this instrumentation in the list of
[tested instrumentations](https://github.com/grafana/grafana-opentelemetry-java/blob/main/custom/src/main/java/com/grafana/extensions/instrumentations/Instrumentations.java).

Where you can find the instrumentation scope:

- for traces - look in tempo plugin in grafana
- for metrics - look at the debug log output in output.log

## Releasing

See [RELEASING](RELEASING.md).

## Known Issues

The tests occasionally fail due to TestContainers not starting in time. Please rerun the build for now, until a new
wait strategy can be determined.
