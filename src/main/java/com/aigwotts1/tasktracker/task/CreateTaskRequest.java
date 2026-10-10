package com.aigwotts1.tasktracker.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        //Bean Validation annotations in Java
        @NotBlank @Size(max = 120) String title) {
}
