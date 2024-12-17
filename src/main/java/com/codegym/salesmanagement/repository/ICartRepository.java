package com.codegym.salesmanagement.repository;

import com.codegym.salesmanagement.model.Cart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserId(Long userId);
    Page<Cart> findByUserUsernameContaining(String username, Pageable pageable);
}
