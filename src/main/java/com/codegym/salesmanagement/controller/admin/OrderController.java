package com.codegym.salesmanagement.controller.admin;

import com.codegym.salesmanagement.formatter.DateFormatter;
import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.Order;
import com.codegym.salesmanagement.service.EmailService;
import com.codegym.salesmanagement.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String listOrders(@RequestParam(value = "username", required = false, defaultValue = "") String username,
                             @RequestParam(value = "status", required = false) Order.OrderStatus status,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage;

        if (status != null) {
            ordersPage = orderService.getOrdersByStatus(status, page, username, pageable);
        } else {
            ordersPage = orderService.getOrders(page, username, pageable);
        }

        for (Order order : ordersPage.getContent()) {
            order.setFormattedTotalAmount(PriceFormatter.formatPrice(order.getTotalAmount()));
        }

        int totalPages = ordersPage.getTotalPages();
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("username", username);
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("status", status);
        return "admin/order/list";
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return "error/404";
        }
        order.setFormattedTotalAmount(PriceFormatter.formatPrice(order.getTotalAmount()));

        order.getOrderItems().forEach(item -> {
            item.setFormattedPrice(PriceFormatter.formatPrice(item.getPrice()));
            item.setFormattedTotal(PriceFormatter.formatPrice(item.getTotal()));
        });

        LocalDateTime orderDate = order.getOrderDate();
        String formattedOrderDate = DateFormatter.formatDateTime(orderDate);

        model.addAttribute("order", order);
        model.addAttribute("formattedOrderDate", formattedOrderDate);
        model.addAttribute("status", Order.OrderStatus.values());
        model.addAttribute("orderItems", order.getOrderItems());
        model.addAttribute("user", order.getUser());
        return "admin/order/detail";
    }

    @PostMapping("/{id}/updateStatus")
    public String updateOrderStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy đơn hàng.");
                return "redirect:/admin/orders";
            }

            Order.OrderStatus currentStatus = order.getStatus();
            orderService.updateOrderStatusToNext(id);

            if (currentStatus == Order.OrderStatus.PROCESSING && order.getStatus() == Order.OrderStatus.SHIPPED) {
                emailService.sendOrderShippedEmail(order);
            }
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái đơn hàng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrderCompletion(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy đơn hàng.");
                return "redirect:/orders";
            }

            if (order.getStatus() == Order.OrderStatus.SHIPPED) {
                order.setStatus(Order.OrderStatus.COMPLETED);
                orderService.save(order);
                redirectAttributes.addFlashAttribute("message", "Cảm ơn bạn đã xác nhận đơn hàng!");
            } else {
                redirectAttributes.addFlashAttribute("message", "Không thể xác nhận đơn hàng ở trạng thái hiện tại.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null) {
                redirectAttributes.addFlashAttribute("message", "Không tìm thấy đơn hàng.");
                return "redirect:/admin/orders";
            }

            if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                redirectAttributes.addFlashAttribute("message", "Không thể hủy đơn hàng đã hoàn thành.");
            } else {
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderService.save(order);
                redirectAttributes.addFlashAttribute("message", "Đơn hàng đã được hủy thành công.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}
