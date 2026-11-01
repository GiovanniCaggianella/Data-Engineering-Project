package it.romesensors.consumer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfiguration {

    @Bean
    public NewTopic measurementsTopic(
            @Value("${app.topic}") String topic,
            @Value("${app.retention-ms}") String retention) {
        return topic(topic, retention);
    }

    @Bean
    public NewTopic deadLetterTopic(
            @Value("${app.topic}") String topic,
            @Value("${app.retention-ms}") String retention) {
        return topic(topic + ".DLT", retention);
    }

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, String> kafka,
            @Value("${app.retry-delay-ms}") long delay,
            @Value("${app.retry-count}") long retries) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafka,
                (record, exception) -> new TopicPartition(
                        record.topic() + ".DLT", record.partition()));
        recoverer.setFailIfSendResultIsError(true);

        DefaultErrorHandler handler = new DefaultErrorHandler(
                recoverer, new FixedBackOff(delay, retries));
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        return handler;
    }

    private NewTopic topic(String name, String retention) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", retention)
                .build();
    }
}
