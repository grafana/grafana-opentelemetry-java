package com.grafana.extensions.resources.internal;

import com.grafana.extensions.resources.DistributionResource;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.resources.Resource;

@SuppressWarnings("rawtypes")
public class DistroComponentProvider implements ComponentProvider<Resource> {

  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "grafana-javaagent-distribution";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    return DistributionResource.get();
  }
}
