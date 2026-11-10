package com.aigwotts1.notifications.controller;

import com.aigwotts1.notifications.dto.Notification;
import com.aigwotts1.notifications.service.NotificationService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // Notifications now arrive from Kafka. HTTP remains for inspecting the result.
    @GetMapping
    public List<Notification> list() {
        return service.findAll();
    }
}
