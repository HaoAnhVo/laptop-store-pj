package com.codegym.salesmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public String showLoginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            String errorMessage = switch (error) {
                // Error Login
                case "notActivated" -> "Tài khoản chưa được xác thực. Vui lòng kiểm tra email để xác nhận tài khoản.";
                case "locked" -> "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ với quản trị viên.";
                case "true" -> "Sai tên tài khoản hoặc mật khẩu.";
                // Login required
                case "login_required" -> "Bạn cần đăng nhập để truy cập đường dẫn này.";
                case "cart_required" -> "Bạn cần đăng nhập để thao tác với giỏ hàng.";
                case "review_required" -> "Bạn cần đăng nhập để đánh giá sản phẩm.";
                case "order_required" -> "Bạn cần đăng nhập để xem danh sách đơn hàng.";
                case "notification_required" -> "Bạn cần đăng nhập để xem danh sách thông báo.";
                case "profile_required" -> "Bạn cần đăng nhập để chỉnh sửa thông tin cá nhân.";
                default -> null;
            };
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
            }
        }
        return "login";
    }

}
