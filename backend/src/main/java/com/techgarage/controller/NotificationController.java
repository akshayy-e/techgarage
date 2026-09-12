package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.notification.NotificationResponse;
import com.techgarage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMine() {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getMyNotifications()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", notificationService.unreadCount())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read", null));
    }
}
