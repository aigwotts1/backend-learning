package com.aigwotts1.tasktracker;

import com.aigwotts1.tasktracker.task.dto.Task;
import com.aigwotts1.tasktracker.task.service.TaskService;
import com.aigwotts1.tasktracker.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import com.aigwotts1.tasktracker.notification.CompletionEventPublisher;
import com.aigwotts1.tasktracker.notification.TaskCompletedEvent;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.KafkaException;
import static org.mockito.Mockito.*;
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
    @MockitoBean CompletionEventPublisher eventPublisher;
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

    @Test
    void publishesOnlyOnCommittedCompletionTransitions() {
        Task task = service.create("Learn service communication");
        try {
            var transaction = new TransactionTemplate(transactionManager);
            transaction.executeWithoutResult(status -> {
                service.complete(task.id());
                verifyNoInteractions(eventPublisher);
                status.setRollbackOnly();
            });
            verifyNoInteractions(eventPublisher);
            assertThat(service.findById(task.id()).completed()).isFalse();

            service.complete(task.id());
            service.complete(task.id());
            service.update(task.id(), task.title(), true);
            verify(eventPublisher, times(1)).send(argThat(event -> event.taskId() == task.id() && event.title().equals(task.title())));

            service.update(task.id(), task.title(), false);
            service.update(task.id(), "Completed through PUT", true);
            verify(eventPublisher).send(argThat(event -> event.taskId() == task.id() && event.title().equals("Completed through PUT")));
            verifyNoMoreInteractions(eventPublisher);
        } finally {
            repository.deleteById(task.id());
        }
    }

    @Test
    void kafkaOutageDoesNotUndoTheTask() {
        doThrow(new KafkaException("Service unavailable"))
                .when(eventPublisher).send(any(TaskCompletedEvent.class));
        Task task = service.create("Keep the task committed");
        try {
            service.complete(task.id());
            assertThat(service.findById(task.id()).completed()).isTrue();
            verify(eventPublisher).send(argThat(event -> event.taskId() == task.id() && event.title().equals(task.title())));
        } finally {
            repository.deleteById(task.id());
        }
    }
}
