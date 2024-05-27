# Changelog

## 2.4.0-beta.1 (2024-05-27)

- Update to [OpenTelemetry 2.4.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/CHANGELOG.md#version-240-2024-05-18)

## 2.3.0-beta.1 (2024-04-12)

- Update to [OpenTelemetry 2.3.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/CHANGELOG.md#version-230-2024-04-12)

## 2.2.0-beta.1 (2024-03-15)

- Update to [OpenTelemetry 2.2.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/CHANGELOG.md#version-220-2024-03-14)
  - The resource providers for Google Cloud and AWS are not enabled by default anymore.
    You can enable them using `OTEL_RESOURCE_PROVIDERS_AWS_ENABLED=true` and `OTEL_RESOURCE_PROVIDERS_GCP_ENABLED=true`.
  - Jetty is now supported in Spring Boot 3.2.

## 2.1.0-beta.1 (2024-02-19)

- Update to [OpenTelemetry 2.1.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/CHANGELOG.md#version-210-2024-02-15)
  - Actuator instrumentation has been disabled by default. You can enable using
    `OTEL_INSTRUMENTATION_SPRING_BOOT_ACTUATOR_AUTOCONFIGURE_ENABLED=true` or
    `-Dotel.instrumentation.spring-boot-actuator-autoconfigure.enabled=true`.

## 2.0.0-beta.1 (2024-01-17)

- Update to [OpenTelemetry 2.0.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/CHANGELOG.md#version-200-2024-01-12)
  - Many **Breaking Change**, most notably:
    - The default OTLP protocol has been changed from `grpc` to `http/protobuf` in order to align with
        the [specification](https://github.com/open-telemetry/opentelemetry-specification/blob/v1.28.0/specification/protocol/exporter.md#specify-protocol).
        You can switch to the `grpc` protocol using `OTEL_EXPORTER_OTLP_PROTOCOL=grpc`
        or `-Dotel.exporter.otlp.protocol=grpc`.
    - Micrometer metric bridge has been disabled by default. You can enable it using
        `OTEL_INSTRUMENTATION_MICROMETER_ENABLED=true`
        or `-Dotel.instrumentation.micrometer.enabled=true`.
  - Stable JVM semantic conventions are now emitted by default - but this was already the case in 0.32.0-beta.1
     (via opt-in), so no change here.
- (Informational, not related to this release) Spring Boot 3.2 is supported, except for Jetty
  (see [compatibility matrix](README.md#compatibility)).
- The following `GRAFANA_*` environment variables are now deprecated and will be removed in a future release.
  Please use `OTEL_*` environment variables instead
  (in the spirit of making it easy to migrate away from this distribution).
  - `GRAFANA_OTLP_DEBUG_LOGGING` and `GRAFANA_OTLP_LOGGING_EXPORTER_ENABLED`
      ([details](README.md#enable-otlp-debug-logging))
  - `GRAFANA_CLOUD_INSTANCE_ID`, `GRAFANA_CLOUD_ZONE`, and `GRAFANA_CLOUD_API_KEY`
      ([details](README.md#grafana-cloud-otlp-gateway))

## 0.32.0-beta.1 (2023-11-22)

- Update to OpenTelemetry 1.32.0
- **Breaking Change**: Use new [JVM semantic conventions](https://opentelemetry.io/docs/specs/semconv/runtime/jvm-metrics/)
- **Breaking Change**: Use `telemetry.distro.name` = `grafana-opentelemetry-java`
  and `telemetry.distro.version` = `0.32.0-beta.1` to identify this distribution instead of
  `telemetry.sdk.name` = `grafana-opentelemetry-java` and `telemetry.sdk.version` = `0.32.0`
  (for reference: from now on `telemetry.sdk.name` = `opentelemetry` and `telemetry.sdk.version` = `1.32.0`.

## 0.31.0 (2023-11-13)

- This is the **first public preview release** of this distribution which is released together with the
  public release of
  [Grafana Cloud Application Observability](https://grafana.com/docs/grafana-cloud/monitor-applications/application-observability/).
- This release is based on OpenTelemetry 1.31.0 - so the version number becomes 0.31.0
  (we'll switch to 1.x.y with the first GA release).
- No functional changes.

## 0.3.0 (2023-11-08)

- Add resource detectors for Google Cloud and AWS - mainly for Kubernetes monitoring.
- Add ability to drop metrics that are not needed for Application Observability ([docs](README.md#data-saver)).
- Rename grafana cloud environment variables `GRAFANA_OTLP_CLOUD_*` to `GRAFANA_CLOUD_*`.

## 0.2.0 (2023-10-27)

- Update to OpenTelemetry 1.31.0
