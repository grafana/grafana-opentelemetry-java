/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ResourceAttributes;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ServiceInstanceIdResourceTest {

  record ServiceInstanceIdTestCase(Map<String, String> resource, String want) {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("serviceInstanceIdTestCases")
  void serviceInstanceId(String name, ServiceInstanceIdTestCase testCase) {
    Resource r =
        ServiceInstanceIdResource.getResource(
            DefaultConfigProperties.createFromMap(
                Map.of(
                    ServiceInstanceIdResource.RESOURCE_ATTRIBUTES,
                    testCase.resource.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(",")))));
    assertThat(r.getAttribute(ResourceAttributes.SERVICE_INSTANCE_ID)).matches(testCase.want);
  }

  public static Stream<Arguments> serviceInstanceIdTestCases() {
    return Stream.of(
        Arguments.of(
            "service instance id is not set, but pod name and container name are",
            new ServiceInstanceIdTestCase(
                Map.of(
                    ResourceAttributes.K8S_POD_NAME.getKey(),
                    "pod-12345",
                    ResourceAttributes.K8S_CONTAINER_NAME.getKey(),
                    "container-42"),
                "pod-12345/container-42")),
        Arguments.of(
            "fall back to random service instance id",
            new ServiceInstanceIdTestCase(
                Map.of(ResourceAttributes.K8S_POD_NAME.getKey(), "pod-12345"),
                "........-....-....-....-............")));
  }
}
