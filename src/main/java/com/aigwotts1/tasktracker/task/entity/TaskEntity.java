package com.aigwotts1.tasktracker.task.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     Strategy	Description
// IDENTITY	Database generates ID on insert
// SEQUENCE	Uses a database sequence
// TABLE	Uses a table to allocate identifiers
// AUTO	Provider chooses a strategy
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    public TaskEntity(String title) {
        this.title = title;
        this.completed = false;
    }
}
