package com.aigwotts1.tasktracker.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TaskCompletionListener {
    private static final Logger log = LoggerFactory.getLogger(TaskCompletionListener.class);
    private final CompletionEventPublisher publisher;

    public TaskCompletionListener(CompletionEventPublisher publisher) {
        this.publisher = publisher;
    }

    // Publishing happens after commit. A Kafka outage cannot undo the saved task.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompleted(TaskCompletedEvent event) {
        try {
            publisher.send(event);
        } catch (KafkaException | IllegalStateException exception) {
            log.warn("Could not publish event {} for task {} ({}).",
                    event.eventId(), event.taskId(), exception.getClass().getSimpleName());
        }
    }
}
