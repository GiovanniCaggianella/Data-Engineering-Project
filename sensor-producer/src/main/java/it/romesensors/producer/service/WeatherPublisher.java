package it.romesensors.producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.romesensors.producer.model.District;
import it.romesensors.producer.model.HistoricalData;
import it.romesensors.producer.model.Observation;
import it.romesensors.producer.model.WeatherEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class WeatherPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(WeatherPublisher.class);

    private final WeatherSource source;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final Map<District, HistoricalData> data = new HashMap<District, HistoricalData>();
    private final Map<District, Integer> positions = new HashMap<District, Integer>();
    private boolean finished;

    public WeatherPublisher(
            WeatherSource source,
            KafkaTemplate<String, String> kafka,
            ObjectMapper objectMapper,
            @Value("${app.topic}") String topic) {
        this.source = source;
        this.kafka = kafka;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${app.publish-interval-ms}", initialDelay = 1000)
    public void publishNext() {
        if (finished) {
            return;
        }

        boolean pending = false;
        for (District district : District.ALL) {
            try {
                pending = publish(district) || pending;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception exception) {
                pending = true;
                LOG.warn("Publication failed for {}: {}", district.getName(), exception.getMessage());
            }
        }

        if (!pending) {
            finished = true;
            LOG.info("Historical data publication completed");
        }
    }

    private boolean publish(District district) throws Exception {
        HistoricalData districtData = data.get(district);
        if (districtData == null) {
            districtData = source.load(district);
            data.put(district, districtData);
        }

        Integer savedPosition = positions.get(district);
        int position = savedPosition == null ? 0 : savedPosition.intValue();
        if (position >= districtData.getObservations().size()) {
            return false;
        }

        Observation observation = districtData.getObservations().get(position);
        WeatherEvent event = WeatherEvent.from(
                district, observation, source.getReference(district), districtData);
        kafka.send(topic, district.getSensorId(), objectMapper.writeValueAsString(event))
                .get(20, TimeUnit.SECONDS);
        positions.put(district, Integer.valueOf(position + 1));
        return true;
    }
}
