package com.aigwotts1.tasktracker.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @NotBlank @Size(max = 120) String title,
        @NotNull Boolean completed) {
}
