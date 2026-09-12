package com.techgarage.service.impl;

import com.techgarage.dto.notification.NotificationResponse;
import com.techgarage.entity.Notification;
import com.techgarage.entity.User;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.NotificationRepository;
import com.techgarage.repository.UserRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Override
    public void notify(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        User me = securityUtil.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(me.getId()).stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public long unreadCount() {
        User me = securityUtil.getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(me.getId());
    }
}
