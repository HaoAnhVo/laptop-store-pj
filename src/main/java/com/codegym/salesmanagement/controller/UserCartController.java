package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.Cart;
import com.codegym.salesmanagement.model.CartItem;
import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.CartItemService;
import com.codegym.salesmanagement.service.CartService;
import com.codegym.salesmanagement.service.ProductService;
import com.codegym.salesmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class UserCartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));

        Cart cart = cartService.getCartByUserId(user.getId());
        if (cart == null) {
            cart = cartService.createCart(user);
        } else {
            cartItemService.removeOutOfStockItems(cart);
        }

        BigDecimal totalAmount = cartService.getTotalAmount(cart);
        String formattedTotalAmount = PriceFormatter.formatPrice(totalAmount);

        for (CartItem item : cart.getCartItems()) {
            BigDecimal total = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            item.setFormattedPrice(PriceFormatter.formatPrice(item.getPrice()));
            item.setFormattedTotal(PriceFormatter.formatPrice(total));
        }

        // Thêm giỏ hàng và tổng giá trị vào model
        model.addAttribute("cart", cart);
        model.addAttribute("totalAmount", formattedTotalAmount);
        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));

            Cart cart = cartService.getCartByUserId(user.getId());

            if (cart == null) {
                cart = cartService.createCart(user);
            }

            Product product = productService.getProductById(productId);
            if (product == null || product.getStock() == 0) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm đã hết hàng!");
                return "redirect:/";
            }

            CartItem existingCartItem = cartService.findCartItemByProduct(cart, product);
            if (existingCartItem != null) {
                existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
                existingCartItem.setTotal(existingCartItem.getPrice().multiply(new BigDecimal(existingCartItem.getQuantity())));
                cartItemService.saveCartItem(existingCartItem);
            } else {
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(1L);
                cartItem.setPrice(product.getPrice());
                cartItem.setTotal(product.getPrice());
                cartItemService.saveCartItem(cartItem);
            }

            cartService.updateTotalAmount(cart);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được thêm vào giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping(value = "/update", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> updateCart(@RequestParam Long cartItemId, @RequestParam Long quantity) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (quantity < 1) {
                response.put("success", false);
                response.put("message", "Số lượng không hợp lệ.");
                return ResponseEntity.badRequest().body(response);
            }

            CartItem cartItem = cartItemService.getCartItemById(cartItemId);
            if (cartItem == null) {
                response.put("success", false);
                response.put("message", "Không có sản phẩm nào trong giỏ hàng.");
                return ResponseEntity.badRequest().body(response);
            }

            Product product = cartItem.getProduct();
            if (quantity > product.getStock()) {
                response.put("success", false);
                response.put("message", "Số lượng vượt quá số lượng sản phẩm trong kho. Tối đa có thể thêm " + product.getStock() + " sản phẩm.");
                return ResponseEntity.badRequest().body(response);
            }

            cartItem.setQuantity(quantity);
            cartItem.setTotal(cartItem.getPrice().multiply(new BigDecimal(quantity)));
            cartItemService.saveCartItem(cartItem);

            Cart cart = cartItem.getCart();
            cartService.updateTotalAmount(cart);
            response.put("success", true);
            response.put("updatedTotal", PriceFormatter.formatPrice(cartItem.getTotal()));
            response.put("cartTotal", PriceFormatter.formatPrice(cartService.getTotalAmount(cart)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/remove")
    public String removeFromCart(@RequestParam Long cartItemId, RedirectAttributes redirectAttributes) {
        try {
            cartItemService.removeCartItem(cartItemId);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa khỏi giỏ hàng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }
}
