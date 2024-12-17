package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.*;
import com.codegym.salesmanagement.repository.ICartItemRepository;
import com.codegym.salesmanagement.repository.ICartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    @Autowired
    private ICartRepository iCartRepository;

    @Autowired
    private ICartItemRepository iCartItemRepository;

    public Page<Cart> getCarts(int page, String username, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return iCartRepository.findByUserUsernameContaining(username, pageable);
        }
        return iCartRepository.findAll(pageable);
    }

    public Cart getCartById(Long cartId) {
        return iCartRepository.findById(cartId).orElse(null);
    }

    public Cart getCartByUserId(Long userId) {
        return iCartRepository.findByUserId(userId);
    }

    public CartItem findCartItemByProduct(Cart cart, Product product) {
        return cart.getCartItems().stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst()
                .orElse(null);
    }

    public void updateTotalAmount(Cart cart) {
        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(CartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);
        iCartRepository.save(cart);
    }

    public BigDecimal getTotalAmount(Cart cart) {
        return cart.getCartItems().stream()
                .map(CartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Cart createCart(User user) {
        Cart existingCart = iCartRepository.findByUserId(user.getId());
        if (existingCart != null) {
            return existingCart;
        }
        // Tạo giỏ hàng mới
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());
        cart.setTotalAmount(BigDecimal.ZERO);

        return iCartRepository.save(cart);
    }

    public void clearCart(Cart cart) {
        cart.getCartItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        iCartRepository.save(cart);
    }

}
