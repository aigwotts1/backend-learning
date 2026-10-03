package com.aigwotts1.tasktracker.task;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    // Milestone 1: temporary storage. Restarting the app clears these tasks.
    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong();

    public Task create(String title) {
        Task task = new Task(nextId.incrementAndGet(), title.strip(), false);
        tasks.put(task.id(), task);
        return task;
    }

    public List<Task> findAll() {
        return tasks.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }
}
