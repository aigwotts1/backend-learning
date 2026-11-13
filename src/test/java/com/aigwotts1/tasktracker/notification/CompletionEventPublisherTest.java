package com.aigwotts1.tasktracker.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CompletionEventPublisherTest {
    @Test
    @SuppressWarnings("unchecked")
    void publishesJsonWithAStableEventIdAndTaskKey() throws Exception {
        KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
        when(kafka.send(anyString(), anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));
        var mapper = new ObjectMapper();
        var publisher = new CompletionEventPublisher(kafka, mapper);
        var event = new TaskCompletedEvent(42, "Learn Kafka", UUID.randomUUID());
        publisher.send(event);
        var payload = ArgumentCaptor.forClass(String.class);
        verify(kafka).send(eq("task-completed"), eq("42"), payload.capture());
        var json = mapper.readTree(payload.getValue());
        assertThat(json.get("eventId").asText()).isEqualTo(event.eventId().toString());
        assertThat(json.get("taskId").asLong()).isEqualTo(42);
        assertThat(json.get("title").asText()).isEqualTo("Learn Kafka");
    }
}
