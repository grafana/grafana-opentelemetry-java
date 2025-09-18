/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources.internal;

import com.grafana.extensions.resources.DistributionResource;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class DistroComponentProvider implements ComponentProvider<Resource> {

  static final String GRAFANA_JAVAAGENT_DISTRIBUTION = "grafana-javaagent-distribution";
  private static final List<String> REQUIRED_DETECTORS =
      Collections.singletonList(GRAFANA_JAVAAGENT_DISTRIBUTION);

  public static void addDistroResourceProvider(OpenTelemetryConfigurationModel model) {
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
  }

  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return GRAFANA_JAVAAGENT_DISTRIBUTION;
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    return DistributionResource.get();
  }
}
