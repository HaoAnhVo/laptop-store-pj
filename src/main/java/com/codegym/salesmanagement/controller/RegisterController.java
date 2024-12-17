package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.EmailService;
import com.codegym.salesmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "error.user", "Tên tài khoản đã tồn tại!");
            return "register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email này đã tồn tại!");
            return "register";
        }

        if (userService.existsByPhone(user.getPhone())) {
            result.rejectValue("phone", "error.user", "Số điện thoại này đã tồn tại!");
            return "register";
        }
        user.setPassword(userService.encodePassword(user.getPassword()));
        user.setRole(User.Role.USER);
        user.setEnabled(false);
        User savedUser = userService.createUser(user);

        String verificationToken = userService.createVerificationToken(savedUser);
        String link = "http://localhost:8080/register/verify?token=" + verificationToken;
        emailService.sendVerificationEmail(savedUser.getEmail(), link);

        model.addAttribute("email", user.getEmail());
        return "register-confirmation";
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        boolean verified = userService.verifyToken(token);
        if (verified) {
            userService.enableUserByVerificationToken(token);
            redirectAttributes.addFlashAttribute("message", "Xác nhận email thành công! Bạn có thể đăng nhập.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Liên kết xác nhận không hợp lệ hoặc đã hết hạn.");
            return "redirect:/register";
        }
    }
}
