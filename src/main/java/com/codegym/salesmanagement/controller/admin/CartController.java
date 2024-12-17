package com.codegym.salesmanagement.controller.admin;

import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.Cart;
import com.codegym.salesmanagement.model.CartItem;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/carts")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public String listCarts(@RequestParam(value = "username", required = false, defaultValue = "") String username,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "5") int size,
                            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cart> cartPage = cartService.getCarts(page, username, pageable);

        int totalPages = cartPage.getTotalPages();

        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("cartPage", cartPage);
        model.addAttribute("carts", cartPage.getContent());
        model.addAttribute("username", username);
        model.addAttribute("pageNumbers", pageNumbers);
        return "admin/cart/list";
    }

    @GetMapping("/{cartId}")
    public String viewUserCart(@PathVariable Long cartId, Model model) {
        Cart cart = cartService.getCartById(cartId);
        User user = cart.getUser();
        BigDecimal totalAmount = cartService.getTotalAmount(cart);
        String formattedTotalAmount = PriceFormatter.formatPrice(totalAmount);

        for (CartItem item : cart.getCartItems()) {
            item.setFormattedPrice(PriceFormatter.formatPrice(item.getPrice()));
            BigDecimal total = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
            item.setFormattedTotal(PriceFormatter.formatPrice(total));
        }
        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cart.getCartItems());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("totalAmount", formattedTotalAmount);
        return "admin/cart/detail";
    }
}
