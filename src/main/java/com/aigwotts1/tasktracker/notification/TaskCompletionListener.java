package com.aigwotts1.tasktracker.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;

@Component
public class TaskCompletionListener {
    private static final Logger log = LoggerFactory.getLogger(TaskCompletionListener.class);
    private final NotificationClient client;

    public TaskCompletionListener(NotificationClient client) {
        this.client = client;
    }

    // The local task is already committed. This HTTP call cannot undo that commit.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCompleted(TaskCompletedEvent event) {
        try {
            client.send(event);
        } catch (RestClientException exception) {
            // Milestone 5 is best effort: no retry or durable delivery queue yet.
            log.warn("Notification for completed task {} was not confirmed ({}).",
                    event.taskId(), exception.getClass().getSimpleName());
        }
    }
}
