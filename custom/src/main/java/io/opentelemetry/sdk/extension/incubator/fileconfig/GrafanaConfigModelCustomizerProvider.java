package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.grafana.extensions.resources.DistributionResource;
import com.grafana.extensions.resources.internal.DistributionVersion;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;

import java.util.List;

public class GrafanaConfigModelCustomizerProvider
    implements OpenTelemetryConfigurationModelCustomizerProvider {

  @Override
  public OpenTelemetryConfigurationModel customize(OpenTelemetryConfigurationModel model) {
    List<AttributeNameValueModel> attributes = model
      .getResource()
      .getAttributes();
    attributes
        .add(
            new AttributeNameValueModel()
                .withName(DistributionResource.DISTRIBUTION_NAME.getKey())
                .withType(AttributeNameValueModel.Type.STRING)
                .withValue("grafana-opentelemetry-java"));
    attributes
        .add(
            new AttributeNameValueModel()
                .withName(DistributionResource.DISTRIBUTION_VERSION.getKey())
                .withType(AttributeNameValueModel.Type.STRING)
                .withValue(DistributionVersion.VERSION));

    // there should be a notion of instrumentations that have a name
    // there should be a section for vendor settings
    model.getInstrumentation().getJava().getAdditionalProperties();

    return model;
  }
}
