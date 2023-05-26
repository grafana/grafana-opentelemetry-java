/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.resources;

import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DistributionVersion {
  private static final Logger logger = LoggerFactory.getLogger(DistributionVersion.class);

  private DistributionVersion() {}

  public static String getVersion() {
    String versionResource = "version.properties";
    String version = "unknown";

    try (InputStream in =
        DistributionVersion.class.getClassLoader().getResourceAsStream(versionResource)) {

      Properties properties = new Properties();
      if (in != null) {
        properties.load(in);
        version = properties.getProperty("version");
      } else {
        logger.warn(
            String.format(
                "unable to get resource %s. version will be set to %s", versionResource, version));
      }
    } catch (java.io.IOException ioE) {
      logger.error(
          String.format("error loading properties. version will be set to %s", version), ioE);
    }
    return version;
  }
}