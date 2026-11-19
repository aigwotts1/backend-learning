package com.aigwotts1.tasktracker.notification;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!test")
// configuration for Kafka topics used by the application. Topics are created automatically on startup if they do not exist.
public class KafkaTopicConfiguration {
    @Bean
    NewTopic taskCompletedTopic() {
        return TopicBuilder.name(CompletionEventPublisher.TOPIC).partitions(3).replicas(1).build();
    }
}
