package com.codegym.salesmanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "product_types")
@Data
public class ProductType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_type_id")
    private Long id;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @OneToMany(mappedBy = "productType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;
}

