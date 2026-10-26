package com.aigwotts1.tasktracker.task.controller;

import com.aigwotts1.tasktracker.task.dto.CreateTaskRequest;
import com.aigwotts1.tasktracker.task.dto.Task;
import com.aigwotts1.tasktracker.task.dto.UpdateTaskRequest;
import java.net.URI;
import com.aigwotts1.tasktracker.task.service.TaskService;


import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//Controller including REST Endpoints for the Task entity. It handles HTTP requests related to tasks, such as listing all tasks and creating a new task. The controller uses the TaskService to perform business logic and data access operations.
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> list() {
        return taskService.findAll();
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.create(request.title());
        return ResponseEntity.created(URI.create("/api/tasks/" + task.id())).body(task);
    }

    @GetMapping("/{id}")
    public Task getById(@PathVariable long id) {
        return taskService.findById(id);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable long id, @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(id, request.title(), request.completed());
    }

    @PatchMapping("/{id}/complete")
    public Task complete(@PathVariable long id) {
        return taskService.complete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
