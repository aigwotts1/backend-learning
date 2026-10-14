package com.aigwotts1.tasktracker.task.repository;

import com.aigwotts1.tasktracker.task.entity.TaskEntity;


import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}
