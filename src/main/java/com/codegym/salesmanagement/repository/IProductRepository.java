package com.codegym.salesmanagement.repository;

import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE (:productType IS NULL OR p.productType = :productType) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:productName IS NULL OR p.productName LIKE LOWER(CONCAT('%', :productName, '%')))")
    Page<Product> findByFilters(
            @Param("productType") ProductType productType,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("productName") String productName,
            Pageable pageable);


    List<Product> findByProductTypeId(Long productTypeId);
    long count();
    long countByStock(Long stock);
    List<Product> findByStockLessThanEqual(Long threshold);
}
