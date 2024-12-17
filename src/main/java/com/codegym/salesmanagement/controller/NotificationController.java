package com.codegym.salesmanagement.controller;

import com.codegym.salesmanagement.formatter.DateFormatter;
import com.codegym.salesmanagement.model.Notification;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.service.NotificationService;
import com.codegym.salesmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/notifications")
    public String getNotificationsPage(Model model, Principal principal) {
        String username = principal.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng!"));

        List<Notification> notifications = notificationService.getAllNotifications(user.getUsername());

        for (Notification notification : notifications) {
            notification.setFormattedCreatedAt(DateFormatter.formatDateTime(notification.getCreatedAt()));
        }

        model.addAttribute("notifications", notifications);

        return "notification/list";
    }

    @GetMapping("/notifications/{id}")
    public String viewNotification(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Notification notification = notificationService.getNotificationById(id);

            if (notification != null) {
                notification.setIsRead(true);
                notificationService.saveNotification(notification);
            }

            model.addAttribute("notification", notification);
            return "notification/detail";
        }
        return "redirect:/";
    }
}
