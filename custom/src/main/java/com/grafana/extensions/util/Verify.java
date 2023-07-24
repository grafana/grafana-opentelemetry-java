/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.util;

public class Verify {

  public static void verify(boolean condition, String exMessage, String arg) {
    if (!condition) {
      throw new RuntimeException(String.format(exMessage, arg));
    }
  }
}
