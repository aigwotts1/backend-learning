package com.aigwotts1.notifications.dto;

import java.time.Instant;

public record Notification(long id, long taskId, String message, Instant receivedAt) {
}
