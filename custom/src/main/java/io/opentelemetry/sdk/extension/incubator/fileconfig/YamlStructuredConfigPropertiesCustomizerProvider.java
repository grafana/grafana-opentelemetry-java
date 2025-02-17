package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;

// this class will be part of the SDK - it's put here for the PoC
public interface YamlStructuredConfigPropertiesCustomizerProvider extends Ordered {
  YamlStructuredConfigProperties customize(YamlStructuredConfigProperties properties);
}
