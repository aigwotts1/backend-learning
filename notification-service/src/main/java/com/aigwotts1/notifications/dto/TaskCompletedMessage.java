package com.aigwotts1.notifications.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Independently defined JSON contract; no shared Task Tracker Java classes.
public record TaskCompletedMessage(
        @Positive long taskId, @NotBlank @Size(max = 120) String title,
        @NotNull UUID eventId) {
}
