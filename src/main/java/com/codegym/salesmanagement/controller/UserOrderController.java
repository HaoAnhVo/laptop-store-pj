package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.formatter.DateFormatter;
import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.Order;
import com.codegym.salesmanagement.service.OrderService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/orders")
public class UserOrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public String listUserOrders(Principal principal,
                                 @RequestParam(value = "status", required = false) Order.OrderStatus status,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "9") int size,
                                 Model model) {
        String username = principal.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage;

        if (status != null) {
            ordersPage = orderService.getOrdersByStatus(status, page, username, pageable);
        } else {
            ordersPage = orderService.getOrders(page, username, pageable);
        }

        String formattedOrderDate = "";

        for (Order order : ordersPage.getContent()) {
            order.setFormattedTotalAmount(PriceFormatter.formatPrice(order.getTotalAmount()));
            LocalDateTime orderDate = order.getOrderDate();
            formattedOrderDate = DateFormatter.formatDateTime(orderDate);
        }


        int totalPages = ordersPage.getTotalPages();
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("formattedOrderDate", formattedOrderDate);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("status", status);
        return "order/list";
    }

    @GetMapping("/{id}")
    public String viewUserOrderDetails(@PathVariable("id") Long id, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderById(id);
        if (order == null || !order.getUser().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem đơn hàng này.");
            return "redirect:/user/orders";
        }

        order.setFormattedTotalAmount(PriceFormatter.formatPrice(order.getTotalAmount()));
        order.getOrderItems().forEach(item -> {
            item.setFormattedPrice(PriceFormatter.formatPrice(item.getPrice()));
            item.setFormattedTotal(PriceFormatter.formatPrice(item.getTotal()));
        });

        String formattedOrderDate = DateFormatter.formatDateTime(order.getOrderDate());
        model.addAttribute("order", order);
        model.addAttribute("formattedOrderDate", formattedOrderDate);
        model.addAttribute("orderItems", order.getOrderItems());
        return "order/detail";
    }

}
