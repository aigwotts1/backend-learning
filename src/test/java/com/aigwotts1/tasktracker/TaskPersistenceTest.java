package com.aigwotts1.tasktracker;

import com.aigwotts1.tasktracker.task.dto.Task;
import com.aigwotts1.tasktracker.task.service.TaskService;
import com.aigwotts1.tasktracker.task.repository.TaskRepository;
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

    @Test
    void commitsUpdatesCompletionAndDeletion() {
        Task saved = service.create("Original title");
        try {
            // Each service call opens and commits its own transaction.
            service.update(saved.id(), " Updated title ", true);
            Task updated = service.findById(saved.id());
            assertThat(updated.title()).isEqualTo("Updated title");
            assertThat(updated.completed()).isTrue();

            service.update(saved.id(), "Updated title", false);
            assertThat(service.findById(saved.id()).completed()).isFalse();
            service.complete(saved.id());
            assertThat(service.findById(saved.id()).completed()).isTrue();

            service.delete(saved.id());
            assertThat(repository.findById(saved.id())).isEmpty();
        } finally {
            repository.deleteById(saved.id());
        }
    }
}
