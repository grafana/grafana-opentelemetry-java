package com.grafana.extensions.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

public class HighCpuDetector {
  private final double cpuUtilizationThreshold;
  private double cpuUtilization;

  public HighCpuDetector(double cpuUtilizationThreshold) {
    this.cpuUtilizationThreshold = cpuUtilizationThreshold;
  }

  public void setCpuUtilization(double cpuUtilization) {
    this.cpuUtilization = cpuUtilization;
  }

  Attributes getSampledReason() {
    boolean highCpu = cpuUtilization > cpuUtilizationThreshold;
    if (highCpu) {
      return SampleReason.create(
          "high_cpu", Attributes.of(AttributeKey.doubleKey("cpuUtilization"), cpuUtilization));
    }
    return null;
  }
}
