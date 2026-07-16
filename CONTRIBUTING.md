# Contributing

This project is a javaagent distribution of the
OpenTelemetry Java instrumentation agent. It is modeled
after the [OpenTelemetry Java instrumentation agent distro
template](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/examples/distro/README.md).

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

- Java and repo lint fixes: `mise run lint:fix`
- Markdown lint: `mise run lint`
- Markdown lint fixes: `mise run lint:fix`
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
[tested instrumentations](custom/src/main/java/com/grafana/extensions/instrumentations/Instrumentations.java).

Where you can find the instrumentation scope:

- for traces - look in tempo plugin in grafana
- for metrics - look at the debug log output in output.log

## Releasing

See [RELEASING](RELEASING.md).

## Known Issues

The tests occasionally fail due to TestContainers not
starting in time. Please rerun the build for now, until a
new wait strategy can be determined.

## Submit a pull request

Effective 2026-06-22, all Grafana Labs repositories [require signed commits][signed-commits].
To learn more about Git commit verification, refer to [About commit signature verification][signing-commits]
and [Checking your commit signature verification status][verifying-commits].

> [!NOTE]
> Pull requests containing any unsigned commits cannot be merged until all commits are signed.

[signed-commits]: https://docs.github.com/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches#require-signed-commits
[signing-commits]: https://docs.github.com/authentication/managing-commit-signature-verification/about-commit-signature-verification
[verifying-commits]: https://docs.github.com/authentication/troubleshooting-commit-signature-verification/checking-your-commit-and-tag-signature-verification-status
