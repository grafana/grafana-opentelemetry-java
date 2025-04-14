/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.smoketest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.grafana.extensions.filter.DefaultMetrics;
import com.grafana.extensions.resources.internal.DistributionVersion;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Metric;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

class SpringBootSmokeTest extends SmokeTest {

  private static final String HTTP_SERVER_REQUEST_DURATION = "http.server.request.duration";

  @Override
  protected String getTargetImage(int jdk) {
    return "ghcr.io/open-telemetry/opentelemetry-java-instrumentation/smoke-test-spring-boot:jdk"
        + jdk
        + "-20240214.7897623950";
  }

  @Test
  public void checkDistributionVersion() throws IOException, InterruptedException {
    startTarget("-Dgrafana.otel.use-tested-instrumentations=true");

    String response = makeGreetCall();

    Collection<ExportTraceServiceRequest> traces = waitForTraces();

    assertThat(response).isEqualTo("Hi!");
    assertThat(countSpansByName(traces, "GET /greeting")).isOne();
    assertThat(countSpansByName(traces, "WebController.withSpan")).isOne();
    assertThat(
            countResourcesByValue(traces, "telemetry.distro.version", DistributionVersion.VERSION))
        .isGreaterThan(0);
    assertThat(countResourcesByValue(traces, "telemetry.distro.name", "grafana-opentelemetry-java"))
        .isGreaterThan(0);

    assertThat(getLogMessages(waitForLogs())).contains("HTTP request received");
    assertThat(getMetricNames(waitForMetrics())).contains("jvm.memory.committed");
  }

  private String makeGreetCall() {
    String url = String.format("http://localhost:%d/greeting", target.getMappedPort(8080));
    Request request = new Request.Builder().url(url).get().build();

    return makeCall(request);
  }

  @Test
  public void applicationObservabilityMetrics() throws IOException, InterruptedException {
    startTarget("-Dgrafana.otel.application-observability-metrics=true");

    makeGreetCall();

    List<String> metricNames = getMetricNames(waitForMetrics());
    assertThat(metricNames).contains("jvm.memory.used");

    // checked below
    metricNames.remove(HTTP_SERVER_REQUEST_DURATION);

    // all other metrics should have been filtered out
    assertThat(DefaultMetrics.DEFAULT_METRICS)
        .containsOnlyOnceElementsOf(new HashSet<>(metricNames));

    assertRequestDuration(5);
  }

  @Test
  public void includeServerAddress() {
    startTarget("-Dgrafana.otel.http-server-request-duration.server-attributes.enabled=true");

    makeGreetCall();

    assertRequestDuration(7);
  }

  private void assertRequestDuration(int expectedAttributes) {
    Optional<Metric> metricOptional =
        await()
            .atMost(10, SECONDS)
            .until(
                () -> {
                  Collection<ExportMetricsServiceRequest> metrics = waitForMetrics();
                  return getMetricsStream(metrics)
                      .filter(metric -> metric.getName().equals(HTTP_SERVER_REQUEST_DURATION))
                      .findFirst();
                },
                Optional::isPresent);

    List<String> attributes =
        metricOptional.get().getHistogram().getDataPoints(0).getAttributesList().stream()
            .map(KeyValue::getKey)
            .toList();

    assertThat(attributes)
        .containsAll(
            List.of(
                "http.route",
                "http.request.method",
                "http.response.status_code",
                "network.protocol.version",
                "url.scheme"));
    if (expectedAttributes == 9) {
      assertThat(attributes).containsAll(List.of("server.address", "server.port"));
    }

    assertThat(attributes).hasSize(expectedAttributes);
  }
}
