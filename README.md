<!-- markdownlint-disable -->
<p>
  <img src="https://upload.wikimedia.org/wikipedia/commons/3/3b/Grafana_icon.svg" alt="Grafana logo" height="70"/ >
  <img src="https://opentelemetry.io/img/logos/opentelemetry-logo-nav.png" alt="OpenTelemetry logo" width="70"/ >
</p>
<!-- markdownlint-enable -->

# Grafana OpenTelemetry Distribution for Java

[![Build](https://github.com/grafana/grafana-opentelemetry-java/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/grafana/grafana-opentelemetry-java/actions/workflows/build.yml)
[![Slack](https://img.shields.io/badge/join%20slack-%23app--o11y-brightgreen.svg?logo=slack)](https://grafana.slack.com/archives/C05E87XRK3J)

## About

Grafana Distribution of [OpenTelemetry Instrumentation for Java] -
optimized for [Grafana Cloud Application Observability].

![Application Observability](https://grafana.com/media/blog/otel-distro-java/application-observability-grafana-cloud-overview-go.png)

This project provides a Java agent JAR that can be attached to any Java 8+ application and dynamically
injects bytecode to capture telemetry from a number of popular libraries and frameworks.

Why use this distribution instead of [OpenTelemetry Instrumentation for Java] (upstream) directly?

- **Easy to get started**: This distribution is optimized for [Grafana Cloud Application Observability] -
  you can get started with just a few environment variables.
- **Fully Compatible**: This distribution is fully compatible with upstream -
  you can use all configuration options of upstream.
- **Optimized for Application Observability and Grafana Agent**: No need to tweak any configuration settings
  if you use the latest version of the Grafana Agent.
- **Fast Bug Fixes**: We can fix bugs faster without waiting for the next release of
  upstream.
- **Cost Optimized**: You can save costs by sending only the metrics that are actually used by the dashboards in
  Application Observability ([opt-in](https://grafana.com/docs/grafana-cloud/monitor-applications/application-observability/setup/instrument/java/configuration/#data-saver)).
  
> **Open Source Friendly**:
>
> - You can use [OpenTelemetry Instrumentation for Java] directly for [Grafana Cloud Application Observability] -
>   this distribution is just a convenience wrapper.
>   You can find more information how to send telemetry data to Grafana Cloud Databases
>   [here](https://grafana.com/docs/opentelemetry/collector/send-otlp-to-grafana-cloud-databases/).
> - You can use this distribution for any OpenTelemetry use case, not just Grafana Cloud.
> - You can migrate from this distribution to OpenTelemetry Instrumentation for Java as explained
>   [here](https://grafana.com/docs/grafana-cloud/monitor-applications/application-observability/setup/instrument/java/migrate-upstream/).

## Documentation

The documentation can be found in [Application Observability](https://grafana.com/docs/grafana-cloud/monitor-applications/application-observability/setup/instrument/java).

## Community

To engage with the Grafana Cloud Application Observability community:

- Chat with us on our community Slack channel. To invite yourself to the
  Grafana Slack, visit [https://slack.grafana.com/](https://slack.grafana.com)
  and join the [#application-observability](https://grafana.slack.com/archives/C05E87XRK3J) channel.
- Ask questions on the [Discussions page](https://github.com/grafana/grafana-opentelemetry-java/discussions).
- [File an issue](https://github.com/grafana/grafana-opentelemetry-java/issues/new)
  for bugs, enhancements, and feature suggestions.

[OpenTelemetry Instrumentation for Java]: https://github.com/open-telemetry/opentelemetry-java-instrumentation
[Grafana Cloud Application Observability]: https://grafana.com/docs/grafana-cloud/monitor-applications/application-observability/
