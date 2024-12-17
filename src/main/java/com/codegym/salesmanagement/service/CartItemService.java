package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.Cart;
import com.codegym.salesmanagement.model.CartItem;
import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.repository.ICartItemRepository;
import com.codegym.salesmanagement.repository.ICartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartItemService {
    @Autowired
    private ICartRepository iCartRepository;

    @Autowired
    private ICartItemRepository iCartItemRepository;

    public CartItem getCartItemById(Long cartItemId) {
        return iCartItemRepository.findById(cartItemId).orElse(null);
    }

    public void saveCartItem(CartItem cartItem) {
        iCartItemRepository.save(cartItem);
    }

    public void removeCartItem(Long cartItemId) {
        iCartItemRepository.deleteById(cartItemId);
    }

    public void removeOutOfStockItems(Cart cart) {
        List<CartItem> itemsToRemove = new ArrayList<>();
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (product.getStock() == 0) {
                itemsToRemove.add(item);
            }
        }

        for (CartItem item : itemsToRemove) {
            cart.getCartItems().remove(item);
            iCartItemRepository.delete(item);
        }

        iCartRepository.save(cart);
    }

    public boolean isProductInAnyCart(Long productId) {
        return iCartItemRepository.existsByProductId(productId);
    }

}
