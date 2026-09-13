package com.techgarage.service;

import com.techgarage.dto.notification.NotificationResponse;
import java.util.List;

public interface NotificationService {
    void notify(Long userId, String message);
    List<NotificationResponse> getMyNotifications();
    void markAsRead(Long id);
    long unreadCount();
    void clearMine();
}
