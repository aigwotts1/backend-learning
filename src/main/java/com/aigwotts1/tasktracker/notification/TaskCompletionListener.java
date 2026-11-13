package com.aigwotts1.tasktracker.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//[TaskCompletionListener.java]	Using
 //AFTER_COMMIT so rolled-back changes never trigger Kafka publication. 
 // Spring documentation

@Component
public class TaskCompletionListener {
    private static final Logger log = LoggerFactory.getLogger(TaskCompletionListener.class);
    private final CompletionEventPublisher publisher;

    public TaskCompletionListener(CompletionEventPublisher publisher) {
        this.publisher = publisher;
    }

    // Publishing happens after commit. A Kafka outage cannot undo the saved task.
    //Execute this method only after the database transaction that published the event commits successfully.
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
