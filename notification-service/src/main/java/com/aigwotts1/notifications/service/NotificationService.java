package com.aigwotts1.notifications.service;

import com.aigwotts1.notifications.dto.Notification;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    // Event IDs suppress redelivery duplicates during this process's lifetime.
    // Both notifications and this deduplication map are lost on restart.
    private final ConcurrentHashMap<UUID, Notification> notifications = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    public Notification record(UUID eventId, long taskId, String title) {
        return notifications.computeIfAbsent(eventId, ignored -> new Notification(
                nextId.incrementAndGet(), taskId, "Task completed: " + title.strip(), Instant.now()));
    }

    public List<Notification> findAll() {
        return notifications.values().stream().sorted(Comparator.comparingLong(Notification::id)).toList();
    }
}
