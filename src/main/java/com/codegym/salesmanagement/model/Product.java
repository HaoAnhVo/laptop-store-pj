package com.codegym.salesmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(name = "product_name")
    private String productName;

    @ManyToOne
    @JoinColumn(name = "product_type_id", referencedColumnName = "product_type_id", nullable = false)
    private ProductType productType;

    @Column(name = "description")
    private String description;

    @NotNull(message = "Price is required")
    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Transient
    private String formattedPrice;

    @NotNull(message = "Stock is required")
    @Column(name = "stock")
    private Long stock;

    @Transient
    private Long soldQuantity;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

}
