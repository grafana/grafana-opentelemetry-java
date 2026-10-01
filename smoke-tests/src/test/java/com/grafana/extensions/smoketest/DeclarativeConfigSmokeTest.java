/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.smoketest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.metrics.v1.Metric;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

// https://github.com/grafana/grafana-opentelemetry-java/issues/1382
class DeclarativeConfigSmokeTest extends SmokeTest {

  private static final String CONFIG_FILE = "/declarative-config.yaml";

  @Override
  protected String getTargetImage(int jdk) {
    return "ghcr.io/open-telemetry/opentelemetry-java-instrumentation/smoke-test-spring-boot:jdk"
        + jdk
        + "-20240214.7897623950";
  }

  @Override
  protected void customizeTarget(GenericContainer<?> target) {
    target
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("declarative-config.yaml"), CONFIG_FILE)
        .withEnv("OTEL_CONFIG_FILE", CONFIG_FILE);
  }

  @Test
  void agentStartsWithDeclarativeConfig() throws IOException, InterruptedException {
    startTarget();

    String serverTiming = makeGreetCallForServerTimingHeader();

    assertThat(target.getLogs()).doesNotContain("OpenTelemetry Javaagent failed to start");
    assertThat(serverTiming).startsWith("traceparent;desc=");

    Collection<ExportTraceServiceRequest> traces = waitForTraces();
    assertThat(countSpansByName(traces, "GET /greeting")).isOne();
    assertThat(countResourcesByValue(traces, "service.name", "declarative-config-smoke-test"))
        .isGreaterThan(0);

    // verifies that the view from the config file was applied
    Optional<Metric> requestDuration =
        await()
            .atMost(10, SECONDS)
            .until(
                () ->
                    getMetricsStream(waitForMetrics())
                        .filter(metric -> metric.getName().equals("http.server.request.duration"))
                        .findFirst(),
                Optional::isPresent);
    assertThat(requestDuration.get().getHistogram().getDataPoints(0).getExplicitBoundsList())
        .containsExactly(0.01, 0.05, 0.1, 0.25, 0.5, 1.0, 3.0, 5.0, 10.0, 20.0, 30.0, 60.0);
  }

  private String makeGreetCallForServerTimingHeader() {
    String url = String.format("http://localhost:%d/greeting", target.getMappedPort(8080));
    Request request = new Request.Builder().url(url).get().build();

    return await()
        .atMost(20, SECONDS)
        .ignoreExceptions()
        .until(
            () -> {
              try (Response response = OkHttpUtils.client().newCall(request).execute()) {
                // empty string (not null) when the header is missing, so the assertion reports it
                return response.code() == 200 ? response.header("Server-Timing", "") : null;
              }
            },
            Objects::nonNull);
  }
}
