/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.modules;

import com.grafana.extensions.resources.smoketest.IntegrationTest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.IOException;
import java.util.Collection;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** We use Play as an example of library that is not supported by Grafana Labs currently. */
public class DisableUnsupportedInstrumentationsSmokeTest extends IntegrationTest {

  @Override
  protected String getTargetImage(int jdk) {
    // Play does not support JDK 17, but this test is really not about the JDK version, so we just run it
    // with our 8 build.
    if (jdk != 8) {
      Assumptions.abort("Test only runs with JDK 8");
    }
    return "ghcr.io/open-telemetry/opentelemetry-java-instrumentation/smoke-test-play:jdk"
        + jdk
        + "-20210917.1246460868";
  }

  @Test
  public void unsupportedInstrumentationsAreDisabled() throws IOException, InterruptedException {
    startTargetWithExtendedAgent();

    testAndVerify(0);
  }

  @Test
  public void enableAllInstrumentations() throws IOException, InterruptedException {
    startTargetWithExtendedAgent(
        "-Dotel.instrumentation.common.default-enabled=true -Dgrafana.otel.instrumentation.enable-unsupported-modules=true");

    testAndVerify(1);
  }

  private void testAndVerify(int expectedSpans) throws IOException, InterruptedException {
    String url = String.format("http://localhost:%d/welcome?id=1", target.getMappedPort(8080));
    Request request = new Request.Builder().url(url).get().build();

    Response response = client.newCall(request).execute();

    Collection<ExportTraceServiceRequest> traces = waitForTraces();

    Assertions.assertEquals("Welcome 1.", response.body().string());
    Assertions.assertEquals(expectedSpans, countSpansByName(traces, "GET /welcome"));
  }
}
