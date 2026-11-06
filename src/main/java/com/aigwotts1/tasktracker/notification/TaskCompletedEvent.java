package com.aigwotts1.tasktracker.notification;

// A snapshot of the completed task, not a managed JPA entity.
public record TaskCompletedEvent(long taskId, String title) {
}
