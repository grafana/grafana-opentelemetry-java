/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResourceCustomizerTest {

  private static final Resource RESOURCE =
      Resource.builder()
          .put("key1", "short")
          .put("key2", "l".repeat(2048))
          .put("key3", "l".repeat(2049))
          .put(AttributeKey.longKey("key4"), 42L)
          .build();

  record TestCase(Resource want, ConfigProperties config) {}

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void truncate(String name, TestCase testCase) {
    Resource resource = ResourceCustomizer.truncate(RESOURCE, testCase.config());
    assertThat(resource).isEqualTo(testCase.want());
  }

  private static Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of(
            "default limit",
            new TestCase(
                Resource.builder()
                    .put("key1", "short")
                    .put("key2", "l".repeat(2048))
                    .put("key3", "l".repeat(2048))
                    .put(AttributeKey.longKey("key4"), 42L)
                    .build(),
                DefaultConfigProperties.createFromMap(Collections.emptyMap()))),
        Arguments.of(
            "lower limit",
            new TestCase(
                Resource.builder()
                    .put("key1", "short")
                    .put("key2", "l".repeat(20))
                    .put("key3", "l".repeat(20))
                    .put(AttributeKey.longKey("key4"), 42L)
                    .build(),
                DefaultConfigProperties.createFromMap(
                    Collections.singletonMap(ResourceCustomizer.TRUNCATE_LENGTH, "20")))));
  }
}
