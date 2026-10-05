package com.aigwotts1.tasktracker.task;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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

    private Task toTask(TaskEntity entity) {
        return new Task(entity.getId(), entity.getTitle(), entity.isCompleted());
    }
}
