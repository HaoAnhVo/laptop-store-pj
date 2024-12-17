package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.service.EmailService;
import com.codegym.salesmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/resetPassword")
public class PasswordResetController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/forgot")
    public String showForgotPassword() {
        return "forgot-password-form";
    }

    @PostMapping("/forgot")
    public String processForgotPassword(
            @RequestParam String email,
            RedirectAttributes redirectAttributes, Model model) {

        try {
            String token = userService.generateResetToken(email);
            String resetLink = "http://localhost:8080/resetPassword/reset?token=" + token;
            emailService.sendResetPasswordEmail(email, resetLink);
            redirectAttributes.addFlashAttribute("message", "Một email đặt lại mật khẩu đã được gửi!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "reset-password-confirmation";
    }

    @GetMapping("/reset")
    public String showResetForm(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password-form";
    }

    @PostMapping("/reset")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            userService.resetPasswordWithToken(token, newPassword);
            redirectAttributes.addFlashAttribute("message", "Mật khẩu đã được thay đổi thành công!");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/resetPassword/reset?token=" + token;
        }
    }


}
