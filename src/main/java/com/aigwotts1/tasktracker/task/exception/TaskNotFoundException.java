package com.aigwotts1.tasktracker.task.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(long id) {
        super("Task with ID " + id + " was not found.");
    }
}
