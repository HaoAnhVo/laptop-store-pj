package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.*;
import com.codegym.salesmanagement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String viewOrder(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

        Cart cart = cartService.getCartByUserId(user.getId());

        for (CartItem item : cart.getCartItems()) {
            item.setFormattedPrice(PriceFormatter.formatPrice(item.getPrice()));
            item.setFormattedTotal(PriceFormatter.formatPrice(item.getPrice().multiply(new BigDecimal(item.getQuantity()))));
        }

        BigDecimal totalAmount = cartService.getTotalAmount(cart);
        String formattedTotalAmount = PriceFormatter.formatPrice(totalAmount);

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        model.addAttribute("totalAmount", formattedTotalAmount);

        return "checkout/order";
    }

    @PostMapping
    public String placeOrder(Principal principal, @RequestParam("paymentMethod") String paymentMethodValue, RedirectAttributes redirectAttributes) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));

            Order.PaymentMethod paymentMethod = Order.PaymentMethod.valueOf(paymentMethodValue);
            Cart cart = cartService.getCartByUserId(user.getId());

            Order order = new Order();
            order.setUser(user);
            order.setTotalAmount(cartService.getTotalAmount(cart));
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentMethod(paymentMethod);

            if (paymentMethod == Order.PaymentMethod.CASH_ON_DELIVERY) {
                order.setStatus(Order.OrderStatus.PENDING);
            } else if (paymentMethod == Order.PaymentMethod.BANK_TRANSFER) {
                order.setStatus(Order.OrderStatus.PAID);
            }

            for (CartItem cartItem : cart.getCartItems()) {
                Product product = cartItem.getProduct();

                if (product.getStock() < cartItem.getQuantity()) {
                    redirectAttributes.addFlashAttribute("error", "Sản phẩm '" + product.getProductName() + "' không đủ số lượng trong kho.");
                    return "redirect:/checkout";
                }

                product.setStock(product.getStock() - cartItem.getQuantity());
                productService.saveProduct(product);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getPrice());
                orderItem.setTotal(cartItem.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
                order.getOrderItems().add(orderItem);
            }
            orderService.save(order);
            cartService.clearCart(cart);

            emailService.sendOrderConfirmationEmail(user.getEmail(), order);

            return "checkout/success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/checkout";
        }
    }
}
