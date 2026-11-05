package com.aigwotts1.notifications.controller;

import com.aigwotts1.notifications.dto.CreateNotificationRequest;
import com.aigwotts1.notifications.dto.Notification;
import com.aigwotts1.notifications.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Notification> record(@Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(201).body(service.record(request.taskId(), request.title()));
    }

    @GetMapping
    public List<Notification> list() {
        return service.findAll();
    }
}
