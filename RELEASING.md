# Releasing

1. Go to <https://github.com/grafana/grafana-opentelemetry-java/releases/new>
2. Click on "Choose a tag", enter the tag name (e.g. `v0.1.0`), and click "Create a new tag".
   The version number should be the same as the
   [upstream version](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases) that we are using.
   Bugfix releases should be `v2.3.4.1`, where `2.3.4` is the upstream version.
3. Click on "Generate release notes" to auto-generate the release notes based on the commits since the last release.
   - Exclude the commits that are not relevant for the release notes, such as "Update dependencies".
   - Usually, it's just updating the upstream version.
   - Include a link to the upstream release notes, e.g. "Update to [OpenTelemetry 2.9.0](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/CHANGELOG.md#version-290-2024-10-17)".
4. Click on "Publish release".
5. The actual release is done by GitHub Actions, which will build the distribution and upload it to the release.
