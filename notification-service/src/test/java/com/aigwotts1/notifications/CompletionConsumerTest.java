package com.aigwotts1.notifications;

import com.aigwotts1.notifications.messaging.CompletionConsumer;
import com.aigwotts1.notifications.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CompletionConsumerTest {
    @Test
    void validatesPayloadsAndSuppressesDuplicateEventIds() throws Exception {
        var service = new NotificationService();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var consumer = new CompletionConsumer(new ObjectMapper(), factory.getValidator(), service);
            String json = "{\"taskId\":42,\"title\":\"Learn Kafka\",\"eventId\":\"" + UUID.randomUUID() + "\"}";
            consumer.consume(json);
            consumer.consume(json);
            assertThat(service.findAll()).hasSize(1);
            for (String invalid : new String[]{"{", "null", "{}", "{\"taskId\":0}",
                    "{\"taskId\":42,\"title\":\"Missing event ID\"}"}) {
                assertThatThrownBy(() -> consumer.consume(invalid)).isInstanceOf(IllegalArgumentException.class);
            }
            assertThat(service.findAll()).hasSize(1);
        }
    }
}
