package it.romesensors.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic measurementsTopic(
            @Value("${app.topic}") String topic,
            @Value("${app.retention-ms}") String retention) {
        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", retention)
                .build();
    }
}
