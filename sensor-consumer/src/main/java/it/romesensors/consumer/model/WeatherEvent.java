package it.romesensors.consumer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public final class WeatherEvent {
    private static final Instant START_2025 = Instant.parse("2024-12-31T23:00:00Z");
    private static final Instant END_2025 = Instant.parse("2025-12-31T23:00:00Z");

    private final UUID eventId;
    private final String sensorId;
    private final String district;
    private final Double latitude;
    private final Double longitude;
    private final Instant observedAt;
    private final Instant publishedAt;
    private final Double temperature;
    private final Double relativeHumidity;
    private final Double precipitation;
    private final Double windSpeed;
    private final String dataSource;
    private final String sourceReference;
    private final Double sourceLatitude;
    private final Double sourceLongitude;

    @JsonCreator
    public WeatherEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("sensorId") String sensorId,
            @JsonProperty("district") String district,
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("observedAt") Instant observedAt,
            @JsonProperty("publishedAt") Instant publishedAt,
            @JsonProperty("temperature") Double temperature,
            @JsonProperty("relativeHumidity") Double relativeHumidity,
            @JsonProperty("precipitation") Double precipitation,
            @JsonProperty("windSpeed") Double windSpeed,
            @JsonProperty("dataSource") String dataSource,
            @JsonProperty("sourceReference") String sourceReference,
            @JsonProperty("sourceLatitude") Double sourceLatitude,
            @JsonProperty("sourceLongitude") Double sourceLongitude) {
        this.eventId = eventId;
        this.sensorId = sensorId;
        this.district = district;
        this.latitude = latitude;
        this.longitude = longitude;
        this.observedAt = observedAt;
        this.publishedAt = publishedAt;
        this.temperature = temperature;
        this.relativeHumidity = relativeHumidity;
        this.precipitation = precipitation;
        this.windSpeed = windSpeed;
        this.dataSource = dataSource;
        this.sourceReference = sourceReference;
        this.sourceLatitude = sourceLatitude;
        this.sourceLongitude = sourceLongitude;
    }

    public void validate() {
        if (eventId == null || blank(sensorId) || blank(district)
                || observedAt == null || publishedAt == null
                || blank(dataSource) || blank(sourceReference)) {
            throw new IllegalArgumentException("Missing mandatory fields");
        }
        if (!"Open-Meteo/ERA5".equals(dataSource)) {
            throw new IllegalArgumentException("Unknown data source");
        }

        UUID expectedId = UUID.nameUUIDFromBytes(
                (sensorId + "|" + observedAt + "|" + sourceReference)
                        .getBytes(StandardCharsets.UTF_8));
        if (!expectedId.equals(eventId)) {
            throw new IllegalArgumentException("Invalid event ID");
        }
        if (observedAt.isBefore(START_2025) || !observedAt.isBefore(END_2025)
                || observedAt.getEpochSecond() % 3600 != 0 || observedAt.getNano() != 0) {
            throw new IllegalArgumentException("Expected an hourly observation in 2025");
        }
        if (publishedAt.isBefore(observedAt) || publishedAt.isAfter(Instant.now().plusSeconds(300))) {
            throw new IllegalArgumentException("Invalid publishing timestamp");
        }

        requireRange(latitude, -90, 90);
        requireRange(longitude, -180, 180);
        requireRange(sourceLatitude, -90, 90);
        requireRange(sourceLongitude, -180, 180);
        requireRange(temperature, -90, 60);
        requireRange(relativeHumidity, 0, 100);
        requireRange(precipitation, 0, 500);
        requireRange(windSpeed, 0, 400);
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void requireRange(Double value, double minimum, double maximum) {
        if (value == null || !Double.isFinite(value.doubleValue())
                || value.doubleValue() < minimum || value.doubleValue() > maximum) {
            throw new IllegalArgumentException("Missing or out-of-range value");
        }
    }

    public UUID getEventId() { return eventId; }
    public String getSensorId() { return sensorId; }
    public String getDistrict() { return district; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Instant getObservedAt() { return observedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Double getTemperature() { return temperature; }
    public Double getRelativeHumidity() { return relativeHumidity; }
    public Double getPrecipitation() { return precipitation; }
    public Double getWindSpeed() { return windSpeed; }
    public String getDataSource() { return dataSource; }
    public String getSourceReference() { return sourceReference; }
    public Double getSourceLatitude() { return sourceLatitude; }
    public Double getSourceLongitude() { return sourceLongitude; }
}
