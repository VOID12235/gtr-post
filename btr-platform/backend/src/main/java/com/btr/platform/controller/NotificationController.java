package com.btr.platform.controller;

import com.btr.platform.common.ApiResponse;
import com.btr.platform.entity.Notification;
import com.btr.platform.security.SecurityUtils;
import com.btr.platform.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/api/v1/notifications")
    public ApiResponse<List<Notification>> list() {
        return ApiResponse.ok(notificationService.list(SecurityUtils.current().userId()));
    }

    @PutMapping("/api/v1/notifications/{id}/read")
    public ApiResponse<Void> read(@PathVariable Long id) {
        notificationService.markRead(id, SecurityUtils.current().userId());
        return ApiResponse.ok();
    }
}
