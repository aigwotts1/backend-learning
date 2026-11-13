package com.aigwotts1.tasktracker.notification;

import java.util.UUID;

// A record is a concise, immutable data carrier with automatically generated 
// constructor, getters, equals(), hashCode(), and toString(),
//  while a class is more flexible and supports mutable state and custom behavior.

// One stable event ID per completion transition, retained across producer retries.
// Carrying a snapshot containing taskId, title, and a unique eventId.
public record TaskCompletedEvent(long taskId, String title, UUID eventId) {
    public TaskCompletedEvent(long taskId, String title) {
        this(taskId, title, UUID.randomUUID());
    }
}
