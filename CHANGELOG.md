# Changelog

## 0.200.0-beta.1 (todo)

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
- todo: decide on versioning scheme when upstream bumps major version - is it time to end beta maybe?

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
