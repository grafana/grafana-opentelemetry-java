package io.opentelemetry.sdk.extension.incubator.fileconfig;

public class GrafanaConfigPropertiesCustomizerProvider implements YamlStructuredConfigPropertiesCustomizerProvider{

  @Override
  public YamlStructuredConfigProperties customize(YamlStructuredConfigProperties properties) {
    // working on this unstructured properties seems too error prone
    return properties;
  }
}
