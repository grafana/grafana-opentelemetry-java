/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.smoketest;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.IOException;
import java.util.Collection;
import okhttp3.Request;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** We use Play as an example of library that is not tested by Grafana Labs currently. */
public class TestedInstrumentationsSmokeTest extends SmokeTest {

  @Override
  protected String getTargetImage(int jdk) {
    // Play does not support JDK 17, but this test is really not about the JDK version, so we just
    // run it with our 8 build.
    if (jdk != 8) {
      Assumptions.abort("Test only runs with JDK 8");
    }
    return "ghcr.io/open-telemetry/opentelemetry-java-instrumentation/smoke-test-play:jdk"
        + jdk
        + "-20241022.11450623960";
  }

  @Test
  public void untestedInstrumentationsAreExcluded() throws IOException, InterruptedException {
    startTarget("-Dgrafana.otel.use-tested-instrumentations=true");

    testAndVerify(0);
  }

  @Test
  public void includeAllInstrumentations() throws IOException, InterruptedException {
    startTarget();

    testAndVerify(1);
  }

  private void testAndVerify(int expectedSpans) throws IOException, InterruptedException {
    String url = String.format("http://localhost:%d/welcome?id=1", target.getMappedPort(8080));
    Request request = new Request.Builder().url(url).get().build();

    String response = makeCall(request);

    Collection<ExportTraceServiceRequest> traces = waitForTraces();

    assertThat(response).isEqualTo("Welcome 1.");
    assertThat(countSpansByName(traces, "GET /welcome")).isEqualTo(expectedSpans);
  }
}
