package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;

// this class will be part of the SDK - it's put here for the PoC
public interface OpenTelemetryConfigurationModelCustomizerProvider extends Ordered {
  OpenTelemetryConfigurationModel customize(OpenTelemetryConfigurationModel model);
}
