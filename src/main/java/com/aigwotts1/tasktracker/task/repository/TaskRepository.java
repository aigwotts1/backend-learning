package com.aigwotts1.tasktracker.task.repository;

import com.aigwotts1.tasktracker.task.entity.TaskEntity;


import org.springframework.data.jpa.repository.JpaRepository;


// What you get with JpaRepository
// Feature	Available methods
// CRUD	save(), findById(), deleteById()
// Collections	findAll(), saveAll(), deleteAll()
// Pagination	findAll(Pageable pageable)
// Sorting	findAll(Sort sort)
// Counting	count(), existsById()
// JPA-specific	flush(), saveAndFlush()
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}
