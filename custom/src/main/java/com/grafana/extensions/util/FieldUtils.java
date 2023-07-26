/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.extensions.util;

import java.lang.reflect.Field;
import java.util.Objects;

public class FieldUtils {
  public static Object readDeclaredField(Object target, String fieldName) {
    try {
      Objects.requireNonNull(target, "target");
      Class<?> cls = target.getClass();
      Field field =
          Objects.requireNonNull(
              cls.getDeclaredField(fieldName),
              String.format("Cannot locate declared field %s.%s", cls, fieldName));
      field.setAccessible(true);
      return field.get(target);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
