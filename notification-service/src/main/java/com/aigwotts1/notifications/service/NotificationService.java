package com.aigwotts1.notifications.service;

import com.aigwotts1.notifications.dto.Notification;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    // Independent, in-memory storage. Restarting this service clears its notifications.
    private final ConcurrentSkipListMap<Long, Notification> notifications = new ConcurrentSkipListMap<>();
    private final AtomicLong nextId = new AtomicLong();

    public Notification record(long taskId, String title) {
        long id = nextId.incrementAndGet();
        var notification = new Notification(id, taskId,
                "Task completed: " + title.strip(), Instant.now());
        notifications.put(id, notification);
        return notification;
    }

    public List<Notification> findAll() {
        return List.copyOf(notifications.values());
    }
}
