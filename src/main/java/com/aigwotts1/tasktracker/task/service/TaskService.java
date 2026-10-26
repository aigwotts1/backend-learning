package com.aigwotts1.tasktracker.task.service;

import com.aigwotts1.tasktracker.task.dto.Task;
import com.aigwotts1.tasktracker.task.exception.TaskNotFoundException;
import com.aigwotts1.tasktracker.task.entity.TaskEntity;
import com.aigwotts1.tasktracker.task.repository.TaskRepository;


import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// service with normal methods 
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Task create(String title) {
        TaskEntity entity = repository.save(new TaskEntity(title.strip()));
        return toTask(entity);
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(this::toTask)
                .toList();
    }

    @Transactional(readOnly = true)
    public Task findById(long id) {
        return toTask(requireTask(id));
    }

    @Transactional
    public Task update(long id, String title, boolean completed) {
        TaskEntity entity = requireTask(id);
        entity.setTitle(title.strip());
        entity.setCompleted(completed);
        // Hibernate writes changes to this managed entity when the transaction commits.
        return toTask(entity);
    }

    @Transactional
    public Task complete(long id) {
        TaskEntity entity = requireTask(id);
        entity.setCompleted(true);
        return toTask(entity);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireTask(id));
    }

    private TaskEntity requireTask(long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
    private Task toTask(TaskEntity entity) {
        return new Task(entity.getId(), entity.getTitle(), entity.isCompleted());
    }
}
