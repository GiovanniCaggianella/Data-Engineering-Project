package it.romesensors.consumer.service;

import it.romesensors.consumer.model.WeatherEvent;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeasurementStore {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementStore.class);
    private static final String INSERT =
            "INSERT INTO measurements(event_id, sensor_id, observed_at, published_at, "
            + "temperature, relative_humidity, precipitation, wind_speed, data_source, "
            + "source_reference, source_latitude, source_longitude, "
            + "temperature_alert, wind_alert, precipitation_alert) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
            + "ON CONFLICT (sensor_id, observed_at) DO NOTHING";
    private static final String SENSOR_EXISTS =
            "SELECT count(*) FROM sensors "
            + "WHERE sensor_id = ? AND district = ? "
            + "AND abs(latitude - ?) < 0.000001 "
            + "AND abs(longitude - ?) < 0.000001";

    private final JdbcTemplate database;
    private final double temperatureThreshold;
    private final double windThreshold;
    private final double precipitationThreshold;

    public MeasurementStore(
            JdbcTemplate database,
            @Value("${app.temperature-threshold}") double temperatureThreshold,
            @Value("${app.wind-threshold}") double windThreshold,
            @Value("${app.precipitation-threshold}") double precipitationThreshold) {
        if (!Double.isFinite(temperatureThreshold)
                || !Double.isFinite(windThreshold) || windThreshold < 0
                || !Double.isFinite(precipitationThreshold) || precipitationThreshold < 0) {
            throw new IllegalArgumentException("Invalid thresholds");
        }
        this.database = database;
        this.temperatureThreshold = temperatureThreshold;
        this.windThreshold = windThreshold;
        this.precipitationThreshold = precipitationThreshold;
    }

    @Transactional
    public void save(WeatherEvent event) {
        event.validate();
        validateSensor(event);

        boolean temperatureAlert = event.getTemperature() > temperatureThreshold;
        boolean windAlert = event.getWindSpeed() > windThreshold;
        boolean precipitationAlert = event.getPrecipitation() > precipitationThreshold;

        int inserted = database.update(
                INSERT,
                event.getEventId(),
                event.getSensorId(),
                Timestamp.from(event.getObservedAt()),
                Timestamp.from(event.getPublishedAt()),
                event.getTemperature(),
                event.getRelativeHumidity(),
                event.getPrecipitation(),
                event.getWindSpeed(),
                event.getDataSource(),
                event.getSourceReference(),
                event.getSourceLatitude(),
                event.getSourceLongitude(),
                temperatureAlert,
                windAlert,
                precipitationAlert);

        if (inserted == 0) {
            LOG.debug("Duplicate ignored: {}", event.getEventId());
        } else if (temperatureAlert || windAlert || precipitationAlert) {
            LOG.warn("Threshold exceeded: sensor={} observedAt={}",
                    event.getSensorId(), event.getObservedAt());
        }
    }

    private void validateSensor(WeatherEvent event) {
        Integer matches = database.queryForObject(
                SENSOR_EXISTS,
                Integer.class,
                event.getSensorId(),
                event.getDistrict(),
                event.getLatitude(),
                event.getLongitude());
        if (matches == null || matches.intValue() == 0) {
            throw new IllegalArgumentException("Unknown sensor or incorrect location");
        }
    }
}
