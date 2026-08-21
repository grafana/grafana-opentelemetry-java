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

  private static final String E_ACUTE = "\u00E9";
  private static final String GRINNING_FACE = "\uD83D\uDE00";

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

  @ParameterizedTest(name = "{0}")
  @MethodSource("utf8TestCases")
  void truncateUtf8(String name, String value, int limit, String expected) {
    AttributeKey<String> key = AttributeKey.stringKey("key");
    Resource resource = Resource.builder().put(key, value).build();
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap(ResourceCustomizer.TRUNCATE_LENGTH, Integer.toString(limit)));

    assertThat(ResourceCustomizer.truncate(resource, config).getAttribute(key)).isEqualTo(expected);
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

  private static Stream<Arguments> utf8TestCases() {
    return Stream.of(
        Arguments.of("ASCII below limit", "abc", 4, "abc"),
        Arguments.of("ASCII above limit", "abcde", 4, "abcd"),
        Arguments.of("multibyte characters", E_ACUTE.repeat(4), 4, E_ACUTE.repeat(2)),
        Arguments.of("complete emoji at limit", GRINNING_FACE, 4, GRINNING_FACE),
        Arguments.of("do not split surrogate pair", "a" + GRINNING_FACE, 2, "a"));
  }
}
