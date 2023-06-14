/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources.smoketest;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_VERSION;

import com.grafana.extensions.resources.internal.DistributionVersion;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.IOException;
import java.util.Collection;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpringBootIntegrationTest extends IntegrationTest {

  @Override
  protected String getTargetImage(int jdk) {
    return "ghcr.io/open-telemetry/opentelemetry-java-instrumentation/smoke-test-spring-boot:jdk"
        + jdk
        + "-20211213.1570880324";
  }

  @Test
  public void extensionsAreLoadedFromJar() throws IOException, InterruptedException {
    startTarget("/opentelemetry-extensions.jar");

    testAndVerify();

    stopTarget();
  }

  @Test
  public void extensionsAreLoadedFromFolder() throws IOException, InterruptedException {
    startTarget("/");

    testAndVerify();

    stopTarget();
  }

  @Test
  public void extensionsAreLoadedFromJavaagent() throws IOException, InterruptedException {
    startTargetWithExtendedAgent();

    testAndVerify();

    stopTarget();
  }

  private void testAndVerify() throws IOException, InterruptedException {
    String url = String.format("http://localhost:%d/greeting", target.getMappedPort(8080));
    Request request = new Request.Builder().url(url).get().build();

    String currentAgentVersion = DistributionVersion.VERSION;

    Response response = client.newCall(request).execute();

    //Thread.sleep(90000); // delay to allow metric logging

    Collection<ExportTraceServiceRequest> traces = waitForTraces();

    Assertions.assertEquals("Hi!", response.body().string());
    Assertions.assertEquals(1, countSpansByName(traces, "GET /greeting"));
    Assertions.assertEquals(1, countSpansByName(traces, "WebController.greeting"));
    Assertions.assertEquals(1, countSpansByName(traces, "WebController.withSpan"));
    Assertions.assertNotEquals(
        0, countResourcesByValue(traces, TELEMETRY_SDK_VERSION.getKey(), currentAgentVersion));
    Assertions.assertNotEquals(
        0, countResourcesByValue(traces, TELEMETRY_SDK_NAME.getKey(), "grafana"));
  }
}
