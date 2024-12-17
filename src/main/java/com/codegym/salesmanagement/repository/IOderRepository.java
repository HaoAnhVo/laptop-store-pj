package com.codegym.salesmanagement.repository;


import com.codegym.salesmanagement.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface IOderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserUsernameContaining(String username, Pageable pageable);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    Page<Order> findByStatusAndUserUsernameContaining(Order.OrderStatus status, String username, Pageable pageable);
    long countByStatus(Order.OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    Optional<BigDecimal> calculateTotalRevenue();
    boolean existsByUserId(Long userId);
}
