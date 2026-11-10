package com.aigwotts1.tasktracker.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CompletionEventPublisher {
    public static final String TOPIC = "task-completed";
    private static final Logger log = LoggerFactory.getLogger(CompletionEventPublisher.class);
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public CompletionEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka;
        this.mapper = mapper;
    }

    public void send(TaskCompletedEvent event) {
        try {
            // A task ID key keeps events for the same task on the same partition.
            kafka.send(TOPIC, Long.toString(event.taskId()), mapper.writeValueAsString(event))
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            log.warn("Kafka delivery not confirmed for event {} (task {}).",
                                    event.eventId(), event.taskId());
                        }
                    });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize completion event", exception);
        }
    }
}
