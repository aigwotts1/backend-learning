package com.aigwotts1.notifications.messaging;

import com.aigwotts1.notifications.dto.TaskCompletedMessage;
import com.aigwotts1.notifications.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CompletionConsumer {
    private final ObjectMapper mapper;
    private final Validator validator;
    private final NotificationService notifications;

    public CompletionConsumer(ObjectMapper mapper, Validator validator, NotificationService notifications) {
        this.mapper = mapper;
        this.validator = validator;
        this.notifications = notifications;
    }

    @KafkaListener(topics = "task-completed", groupId = "notification-service")
    public void consume(String json) {
        try {
            var event = mapper.readValue(json, TaskCompletedMessage.class);
            if (event == null || !validator.validate(event).isEmpty()) {
                throw new IllegalArgumentException("Invalid completion event");
            }
            notifications.record(event.eventId(), event.taskId(), event.title());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Malformed completion event", exception);
        }
    }
}
