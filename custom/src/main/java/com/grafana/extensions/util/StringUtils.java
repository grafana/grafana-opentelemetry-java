/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.util;

public class StringUtils {

  public static boolean isNotBlank(final String s) {
    return !isBlank(s);
  }

  public static boolean isBlank(final String s) {
    if (s == null || s.isEmpty()) {
      return true;
    }
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!Character.isWhitespace(c)) {
        return false;
      }
    }
    return true;
  }
}
