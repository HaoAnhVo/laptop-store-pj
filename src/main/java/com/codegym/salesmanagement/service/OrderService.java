package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.*;
import com.codegym.salesmanagement.repository.IOderRepository;
import com.codegym.salesmanagement.repository.IOrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class OrderService {
    @Autowired
    private IOderRepository iOrderRepository;

    @Autowired
    private IOrderItemRepository iOrderItemRepository;

    @Autowired
    private NotificationService notificationService;

    public Page<Order> getOrders(int page, String username, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return iOrderRepository.findByUserUsernameContaining(username, pageable);
        }
        return iOrderRepository.findAll(pageable);
    }

    public Order getOrderById(Long id) {
        return iOrderRepository.findById(id).orElse(null);
    }

    public void save(Order order) {
        iOrderRepository.save(order);
    }

    public Page<Order> getOrdersByStatus(Order.OrderStatus status, int page, String username, Pageable pageable) {
        if (username != null && !username.isEmpty()) {
            return iOrderRepository.findByStatusAndUserUsernameContaining(status, username, pageable);
        }
        return iOrderRepository.findByStatus(status, pageable);
    }


    public void updateOrderStatusToNext(Long id) {
        Order order = iOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng với mã #" + id + " không được tìm thấy"));

        Order.OrderStatus nextStatus = getNextStatus(order.getStatus());
        order.setStatus(nextStatus);
        iOrderRepository.save(order);

        String content;
        switch (nextStatus) {
            case PROCESSING:
                content = "Đơn hàng #" + order.getId() + " đang được xử lý";
                break;
            case SHIPPED:
                content = "Đơn hàng #" + order.getId() + " đang được giao đến bạn";
                break;
            case COMPLETED:
                content = "Đã hoàn thành đơn hàng #" + order.getId();
                break;
            case CANCELLED:
                content = "Đơn hàng #" + order.getId() + " đã bị hủy";
                break;
            default:
                content = "Trạng thái đơn hàng #" + order.getId() + " đã thay đổi";
        }

        Notification notification = new Notification();
        notification.setUser(order.getUser());
        notification.setContent(content);
        notification.setIsRead(false);

        notificationService.saveNotification(notification);
    }

    private Order.OrderStatus getNextStatus(Order.OrderStatus currentStatus) {
        if (currentStatus == null) {
            throw new IllegalArgumentException("Trạng thái đang là null");
        }
        return switch (currentStatus) {
            case PENDING -> Order.OrderStatus.PROCESSING;
            case PROCESSING -> Order.OrderStatus.SHIPPED;
            case SHIPPED -> Order.OrderStatus.COMPLETED;
            default -> throw new IllegalStateException("Không thể xác định trạng thái: " + currentStatus);
        };
    }

    public long countTotalOrders() {
        return iOrderRepository.count();
    }

    public long countPendingOrders() {
        return iOrderRepository.countByStatus(Order.OrderStatus.PENDING);
    }

    public BigDecimal calculateTotalRevenue() {
        return iOrderRepository.calculateTotalRevenue().orElse(BigDecimal.ZERO);
    }

    public boolean isProductInAnyOrder(Long productId) {
        return iOrderItemRepository.existsByProductId(productId);
    }

    public boolean isUserHasOrders(Long userId) {
        return iOrderRepository.existsByUserId(userId);
    }

}
