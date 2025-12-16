# Releasing

> [!IMPORTANT]
> Releases are [immutable][immutable-releases] and cannot be changed or their associated tag
> deleted once published.
>
> However, the description can still be edited to fix any mistakes or omissions after publishing.

## Scheduled Releases

Releases are automatically published on a weekly basis via a
[scheduled GitHub Actions workflow][scheduled-release]. The workflow runs every Friday at 09:00 UTC
and will publish a new release if any changes have been made to the
[`otelInstrumentationVersion` variable][otel-java-instrumentation-version] which tracks the latest
version of the [OpenTelemetry Instrumentation for Java][otel-java-instrumentation-latest] since the
[latest release][latest-release] of the Grafana Java distribution for OpenTelemetry.

The release version will match the version specified by the `otelInstrumentationVersion` variable.

## Manual Releases

1. Open the [Publish Release workflow][publish-release]
1. Click on the **Run workflow** button
1. If required, enter a specific version number (e.g. `x.y.z`) in the version field. If left
   blank, the version will track the version in [`otelInstrumentationVersion`][otel-java-instrumentation-version].
   Bugfix releases should be `x.y.z.1`, where `x.y.z` is the upstream version.
1. Wait for the workflow to complete successfully.
1. Click the link in the workflow run summary to the untagged release created by the workflow.
1. Click the edit button (pencil icon) at the top right of the release notes.
1. Verify that the release notes are correct. Make any manual adjustments if necessary.
   - Include a link to the upstream release notes,
     e.g. _"Update to [OpenTelemetry 2.9.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/CHANGELOG.md#version-290-2024-10-17)"_.
1. Click on **Publish release**.

<!-- editorconfig-checker-disable -->
<!-- markdownlint-disable MD013 -->

[immutable-releases]: https://docs.github.com/code-security/supply-chain-security/understanding-your-software-supply-chain/immutable-releases
[otel-java-instrumentation-latest]: https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest
[otel-java-instrumentation-version]: https://github.com/grafana/grafana-opentelemetry-java/blob/main/build.gradle#L6
[latest-release]: https://github.com/grafana/grafana-opentelemetry-java/releases/latest
[publish-release]: https://github.com/grafana/grafana-opentelemetry-java/actions/workflows/publish-release.yml
[scheduled-release]: https://github.com/grafana/grafana-opentelemetry-java/blob/main/.github/workflows/scheduled-release.yml
