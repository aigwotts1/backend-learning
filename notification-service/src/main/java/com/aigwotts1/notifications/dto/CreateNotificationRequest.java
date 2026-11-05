package com.aigwotts1.notifications.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull @Positive Long taskId,
        @NotBlank @Size(max = 120) String title) {
}
