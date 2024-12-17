package com.codegym.salesmanagement.service;

import com.codegym.salesmanagement.model.Notification;
import com.codegym.salesmanagement.model.User;
import com.codegym.salesmanagement.repository.INotificationRepository;
import com.codegym.salesmanagement.repository.IUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private INotificationRepository iNotificationRepository;

    @Autowired
    private IUserRepository iUserRepository;

    private Long getUserIdByUsername(String username) {
        User user = iUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với username: " + username));
        return user.getId();
    }

    public Notification getNotificationById(Long id) {
        return iNotificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
    }

    public List<Notification> getUnreadNotifications(String username) {
        Long userId = getUserIdByUsername(username);
        return iNotificationRepository.findUnreadNotificationsByUserId(userId);
    }

    public Long getUnreadNotificationCount(String username) {
        Long userId = getUserIdByUsername(username);
        return iNotificationRepository.countUnreadNotificationsByUserId(userId);
    }

    public List<Notification> getAllNotifications(String username) {
        Long userId = getUserIdByUsername(username);
        return iNotificationRepository.findAllNotificationsByUserId(userId);
    }

    public void saveNotification(Notification notification) {
        iNotificationRepository.save(notification);
    }

    public void markAllAsRead(String username) {
        Long userId = getUserIdByUsername(username);
        List<Notification> notifications = iNotificationRepository.findUnreadNotificationsByUserId(userId);
        for (Notification n : notifications) {
            n.setIsRead(true);
        }
        iNotificationRepository.saveAll(notifications);
    }

}
