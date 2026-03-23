/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import org.junit.jupiter.api.Test;

class DistroComponentProviderTest {

  @Test
  void addsDetectorWhenAbsent() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();

    DistroComponentProvider.addDistroResourceProvider(model);

    assertThat(model.getResource().getDetectionDevelopment().getDetectors())
        .hasSize(1)
        .first()
        .satisfies(
            d ->
                assertThat(d.getAdditionalProperties())
                    .containsKey(DistroComponentProvider.GRAFANA_JAVAAGENT_DISTRIBUTION));
  }

  @Test
  void doesNotDuplicateWhenPresent() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    DistroComponentProvider.addDistroResourceProvider(model);
    DistroComponentProvider.addDistroResourceProvider(model);

    assertThat(model.getResource().getDetectionDevelopment().getDetectors()).hasSize(1);
  }

  @Test
  void preservesExistingDetectors() {
    OpenTelemetryConfigurationModel model = new OpenTelemetryConfigurationModel();
    DistroComponentProvider.addDistroResourceProvider(model);

    // Add another detector
    ExperimentalResourceDetectorModel other = new ExperimentalResourceDetectorModel();
    other.getAdditionalProperties().put("other-detector", null);
    model.getResource().getDetectionDevelopment().getDetectors().add(other);

    DistroComponentProvider.addDistroResourceProvider(model);

    assertThat(model.getResource().getDetectionDevelopment().getDetectors()).hasSize(2);
  }
}
