package com.grafana.demo;

import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.*;

@Entity
@Table("product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @org.springframework.data.annotation.Id
    private Long id;

    @Basic(optional = false)
    private String name;

    public Product(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Product() {
    }

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
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
