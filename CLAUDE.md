# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

A javaagent distribution of the [OpenTelemetry Java instrumentation agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation), optimized for Grafana Cloud Application Observability. It wraps the upstream agent and adds Grafana-specific extensions (tested instrumentation filtering, metric filtering, resource attribute truncation, default config).

## Build Commands

```sh
./gradlew build                    # full build (compiles, tests, assembles agent JAR)
./gradlew spotlessApply            # auto-format Java (Google Java Format) and misc files
./gradlew :custom:test             # run unit tests only
./gradlew :custom:test --tests '*MetricFilterTest'  # run a single test class
SMOKE_TEST_JAVA_VERSION=8 ./gradlew :smoke-tests:test  # run smoke tests (requires env var)
```

The final artifact is `agent/build/libs/grafana-opentelemetry-java.jar`.

Smoke tests only run when `SMOKE_TEST_JAVA_VERSION` env var is set (or running in IntelliJ). They use TestContainers and test the entire javaagent with a real application.

## Modules

- **`:custom`** — Grafana extensions: instrumentation filtering, metric filtering, resource truncation, version logging. Entry point is `GrafanaAutoConfigCustomizerProvider` (implements OpenTelemetry's `AutoConfigurationCustomizerProvider` SPI). Has checkstyle enforcement.
- **`:agent`** — Packages the upstream OTEL javaagent + custom extensions into a single shadow JAR via a 3-step process (relocate → isolate → merge) to avoid classpath conflicts.
- **`:smoke-tests`** — Integration tests using TestContainers that run a Spring Boot app with the full javaagent attached.

## Architecture

The distro extends upstream OTEL via the `AutoConfigurationCustomizerProvider` SPI. Key customization points in `:custom`:

- `TestedInstrumentationsCustomizer` — optionally limits active instrumentations to a curated list in `Instrumentations.java` (controlled by `grafana.otel.use-tested-instrumentations` property)
- `MetricsCustomizer` / `MetricFilter` — filters metrics
- `ResourceCustomizer` — truncates resource attribute values (default 2048 chars)
- `DistributionVersion.java` — **auto-generated** by `custom/build.gradle` `manageVersionClass` task; do not edit manually

Package relocation (`gradle/shadow.gradle`) moves OpenTelemetry classes to `io.opentelemetry.javaagent.shaded.*` to prevent conflicts with instrumented application code. Resource providers are excluded from relocation.

## Linting

After modifying non-Java files (Markdown, YAML, Dockerfiles, shell scripts, etc.), always run super-linter via mise:

```sh
mise run lint:super-linter
```

## Code Conventions

- Java 8 compilation target (`-Werror`), Java 17 for tests
- Google Java Format via Spotless, checkstyle (Google style) on `:custom`
- License header: Apache 2.0 / Grafana Labs (enforced by Spotless)
- Package root: `com.grafana.extensions.*`
- JUnit 5 + AssertJ for assertions, LogUnit for log capture

## CI

- PR builds test against Java 8, 11, 17, 21
- Linting: super-linter, lychee (link checker), markdownlint, codespell
- `CHECK_GENERATED_FILES=true` validates that auto-generated code is up to date
- OWASP dependency check fails build on CVE CVSS >= 7.0
- Releases are scheduled weekly (Friday 09:00 UTC) or triggered manually

## Dependency Management

- Upstream OTEL version tracked in `build.gradle` → `otelInstrumentationVersion`
- Renovate manages dependency updates; Dockerfile versions use a custom regular expression manager (see `.github/renovate.json5`)
- `mise.toml` manages tool versions (Java, lychee) and lint tasks
