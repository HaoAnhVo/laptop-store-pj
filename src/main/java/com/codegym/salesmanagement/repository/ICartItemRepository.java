package com.codegym.salesmanagement.repository;

import com.codegym.salesmanagement.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByProductId(Long productId);
}
