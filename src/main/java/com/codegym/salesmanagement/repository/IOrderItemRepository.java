package com.codegym.salesmanagement.repository;

import com.codegym.salesmanagement.model.OrderItem;
import com.codegym.salesmanagement.model.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("SELECT oi.product, SUM(oi.quantity) AS totalSold " +
            "FROM OrderItem oi " +
            "GROUP BY oi.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProductsWithQuantity(Pageable pageable);
    boolean existsByProductId(Long productId);
}
