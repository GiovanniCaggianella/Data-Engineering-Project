package it.romesensors.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.romesensors.consumer.model.WeatherEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MeasurementConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementConsumer.class);

    private final ObjectMapper objectMapper;
    private final MeasurementStore store;

    public MeasurementConsumer(ObjectMapper objectMapper, MeasurementStore store) {
        this.objectMapper = objectMapper.copy()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        this.store = store;
    }

    @KafkaListener(
            id = "measurements",
            topics = "${app.topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void receive(ConsumerRecord<String, String> record) {
        try {
            WeatherEvent event = read(record.value());
            if (!event.getSensorId().equals(record.key())) {
                throw new IllegalArgumentException("Kafka key must match sensor ID");
            }
            store.save(event);
        } catch (JsonProcessingException exception) {
            LOG.warn("Invalid JSON at partition={} offset={}", record.partition(), record.offset());
            throw new IllegalArgumentException("Invalid JSON", exception);
        } catch (IllegalArgumentException exception) {
            LOG.warn("Invalid measurement at partition={} offset={}: {}",
                    record.partition(), record.offset(), exception.getMessage());
            throw exception;
        }
    }

    private WeatherEvent read(String json) throws JsonProcessingException {
        if (json == null) {
            throw new IllegalArgumentException("Null payload");
        }
        WeatherEvent event = objectMapper.readValue(json, WeatherEvent.class);
        if (event == null || event.getSensorId() == null) {
            throw new IllegalArgumentException("Missing sensor ID");
        }
        return event;
    }
}
