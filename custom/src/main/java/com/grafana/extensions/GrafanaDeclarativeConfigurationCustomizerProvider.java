/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions;

import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationCustomizer;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfigurationCustomizerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GrafanaDeclarativeConfigurationCustomizerProvider
    implements DeclarativeConfigurationCustomizerProvider {
  private static final List<String> REQUIRED_DETECTORS =
      Collections.singletonList("grafana-javaagent-distribution");

  @Override
  public void customize(DeclarativeConfigurationCustomizer customizer) {
    customizer.addModelCustomizer(
        model -> {
          ResourceModel resource = model.getResource();
          if (resource == null) {
            resource = new ResourceModel();
            model.withResource(resource);
          }
          ExperimentalResourceDetectionModel detectionModel = resource.getDetectionDevelopment();
          if (detectionModel == null) {
            detectionModel = new ExperimentalResourceDetectionModel();
            resource.withDetectionDevelopment(detectionModel);
          }
          List<ExperimentalResourceDetectorModel> detectors =
              Objects.requireNonNull(detectionModel.getDetectors());
          Set<String> names =
              detectors.stream()
                  .flatMap(detector -> detector.getAdditionalProperties().keySet().stream())
                  .collect(Collectors.toSet());

          for (String name : REQUIRED_DETECTORS) {
            if (!names.contains(name)) {
              ExperimentalResourceDetectorModel detector = new ExperimentalResourceDetectorModel();
              detector.getAdditionalProperties().put(name, null);
              detectors.add(detector);
            }
          }
          return model;
        });
  }
}
