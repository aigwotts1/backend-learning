package com.aigwotts1.notifications;

import com.aigwotts1.notifications.service.NotificationService;
import java.time.Duration;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(partitions = 3, topics = {"task-completed", "task-completed.DLT"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        brokerProperties = {"group.initial.rebalance.delay.ms=0"})
@DirtiesContext
class KafkaDeliveryTest {
    @Autowired KafkaTemplate<String, String> kafka;
    @Autowired EmbeddedKafkaBroker broker;
    @org.springframework.test.context.bean.override.mockito.MockitoSpyBean NotificationService service;

    @Test
    void consumesDuplicatesOnceAndRoutesPoisonMessagesToDeadLetterTopic() throws Exception {
        var properties = KafkaTestUtils.consumerProps("dlt-test-" + UUID.randomUUID(), "false", broker);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try (var deadLetters = new DefaultKafkaConsumerFactory<>(properties,
                new StringDeserializer(), new StringDeserializer()).createConsumer()) {
            broker.consumeFromAnEmbeddedTopic(deadLetters, "task-completed.DLT");
            String json = "{\"taskId\":99,\"title\":\"Kafka integration\",\"eventId\":\"" + UUID.randomUUID() + "\"}";
            kafka.send("task-completed", "99", json).get();
            kafka.send("task-completed", "99", json).get();
            // Same task key means the following record follows both valid copies.
            kafka.send("task-completed", "99", "not-json").get();
            var deadLetter = KafkaTestUtils.getSingleRecord(deadLetters, "task-completed.DLT", Duration.ofSeconds(30));
            assertThat(deadLetter.value()).isEqualTo("not-json");
            await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
                assertThat(service.findAll()).hasSize(1);
                assertThat(service.findAll().get(0).taskId()).isEqualTo(99);
            });
            UUID retryId = UUID.randomUUID();
            doThrow(new IllegalStateException("Temporary processing failure")).doCallRealMethod()
                    .when(service).record(retryId, 100L, "Retry delivery");
            String retryJson = "{\"taskId\":100,\"title\":\"Retry delivery\",\"eventId\":\"" + retryId + "\"}";
            kafka.send("task-completed", "100", retryJson).get();
            await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
                assertThat(service.findAll().stream().filter(item -> item.taskId() == 100).count()).isEqualTo(1);
                verify(service, times(2)).record(retryId, 100L, "Retry delivery");
            });
        }
    }
}
