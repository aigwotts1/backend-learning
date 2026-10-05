package com.aigwotts1.tasktracker;

import com.aigwotts1.tasktracker.task.Task;
import com.aigwotts1.tasktracker.task.TaskService;
import com.aigwotts1.tasktracker.task.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskPersistenceTest {
    @Autowired TaskService service;
    @Autowired TaskRepository repository;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    void readsCommittedTaskInANewTransaction() {
        Task saved = service.create("Persist across transactions");
        try {
            TransactionTemplate transaction = new TransactionTemplate(transactionManager);
            transaction.executeWithoutResult(status -> {
                var reloaded = repository.findById(saved.id()).orElseThrow();
                assertThat(reloaded.getTitle()).isEqualTo(saved.title());
                assertThat(reloaded.isCompleted()).isFalse();
            });
        } finally {
            repository.deleteById(saved.id());
        }
    }
}
