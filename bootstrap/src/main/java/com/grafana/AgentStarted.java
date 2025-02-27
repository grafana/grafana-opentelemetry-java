/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana;

public class AgentStarted {
  @SuppressWarnings("SystemOut")
  public static void run(String agentArgs) {
    boolean debug = false;
    if (!agentArgs.isEmpty()) {
      String[] options = agentArgs.split(",");
      for (String option : options) {
        String[] keyValue = option.split("=");
        if (keyValue.length == 2) {
          if (keyValue[0].equals("grafana.otel.debug-agent-startup")) {
            debug = Boolean.parseBoolean(keyValue[1]);
          } else {
            System.setProperty(keyValue[0], keyValue[1]);
            if (debug) {
              System.out.println("Setting property [" + keyValue[0] + "] = " + keyValue[1]);
            }
          }
        }
      }
    }
  }
}
