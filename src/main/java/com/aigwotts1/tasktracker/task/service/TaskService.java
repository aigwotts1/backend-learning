package com.aigwotts1.tasktracker.task.service;

import com.aigwotts1.tasktracker.task.dto.Task;
import com.aigwotts1.tasktracker.task.exception.TaskNotFoundException;
import com.aigwotts1.tasktracker.task.entity.TaskEntity;
import com.aigwotts1.tasktracker.task.repository.TaskRepository;


import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import com.aigwotts1.tasktracker.notification.TaskCompletedEvent;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// service with normal methods 
public class TaskService {
    private final TaskRepository repository;

    private final ApplicationEventPublisher events;

    public TaskService(TaskRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
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
        boolean wasCompleted = entity.isCompleted();
        entity.setTitle(title.strip());
        entity.setCompleted(completed);
        publishCompletionIfChanged(entity, wasCompleted);
        // Hibernate writes changes to this managed entity when the transaction commits.
        return toTask(entity);
    }

    @Transactional
    public Task complete(long id) {
        TaskEntity entity = requireTask(id);
        boolean wasCompleted = entity.isCompleted();
        entity.setCompleted(true);
        publishCompletionIfChanged(entity, wasCompleted);
        return toTask(entity);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireTask(id));
    }

    //	[TaskService.java](TaskService.java)
    // Detecting a change from incomplete to complete and publishing a local Spring event.
    private void publishCompletionIfChanged(TaskEntity entity, boolean wasCompleted) {
        if (!wasCompleted && entity.isCompleted()) {
            events.publishEvent(new TaskCompletedEvent(entity.getId(), entity.getTitle()));
        }
    }
    private TaskEntity requireTask(long id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
    private Task toTask(TaskEntity entity) {
        return new Task(entity.getId(), entity.getTitle(), entity.isCompleted());
    }
}
