package com.aigwotts1.tasktracker.task;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    protected TaskEntity() {
        // JPA needs a no-argument constructor to load rows.
    }

    public TaskEntity(String title) {
        this.title = title;
        this.completed = false;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public boolean isCompleted() { return completed; }
}
