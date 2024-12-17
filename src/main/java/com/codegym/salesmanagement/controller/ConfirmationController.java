package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.model.Order;
import com.codegym.salesmanagement.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/confirmationOrder")
public class ConfirmationController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirmOrder(@PathVariable Long id, @RequestParam("token") String token) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Đơn hàng không tồn tại.");
        }

        if (!order.getConfirmationToken().equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token không hợp lệ hoặc đã hết hạn.");
        }

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setConfirmationToken(null);
        orderService.save(order);

        return ResponseEntity.ok("Đơn hàng đã được xác nhận hoàn thành. Chúc bạn có một ngày vui vẻ!");
    }
}
