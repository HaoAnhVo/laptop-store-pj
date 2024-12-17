package com.codegym.salesmanagement.controller.admin;

import com.codegym.salesmanagement.formatter.PriceFormatter;
import com.codegym.salesmanagement.model.Product;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.OrderService;
import com.codegym.salesmanagement.service.ProductService;
import com.codegym.salesmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        // User
        long totalUsers = userService.countUsersWithRole(User.Role.USER);

        // Sản phẩm
        long totalProducts = productService.countTotalProducts();
        long outOfStockProducts = productService.countOutOfStockProducts();
        List<Product> lowStockProducts = productService.getLowStockProducts(5L);
        List<Product> topSellingProducts = productService.getTopSellingProducts(5);

        // Đơn hàng
        long totalOrders = orderService.countTotalOrders();
        long pendingOrders = orderService.countPendingOrders();
        BigDecimal totalRevenue = orderService.calculateTotalRevenue();
        String totalRevenueFormatter = PriceFormatter.formatPrice(totalRevenue);

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("topSellingProducts", topSellingProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalRevenue", totalRevenueFormatter);
        return "admin/dashboard";
    }
}
