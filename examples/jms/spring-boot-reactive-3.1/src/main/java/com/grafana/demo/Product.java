/*
 * Copyright Grafana Labs
 * SPDX-License-Identifier: Apache-2.0
 */

package com.grafana.demo;

public class Product {

  private Long id;

  private String name;

  public Product(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Product() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Product{" + "id=" + id + ", name='" + name + '\'' + '}';
  }
}
