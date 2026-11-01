package it.romesensors.producer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public final class WeatherEvent {
    private final UUID eventId;
    private final String sensorId;
    private final String district;
    private final double latitude;
    private final double longitude;
    private final Instant observedAt;
    private final Instant publishedAt;
    private final double temperature;
    private final double relativeHumidity;
    private final double precipitation;
    private final double windSpeed;
    private final String dataSource;
    private final String sourceReference;
    private final double sourceLatitude;
    private final double sourceLongitude;

    @JsonCreator
    public WeatherEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("sensorId") String sensorId,
            @JsonProperty("district") String district,
            @JsonProperty("latitude") double latitude,
            @JsonProperty("longitude") double longitude,
            @JsonProperty("observedAt") Instant observedAt,
            @JsonProperty("publishedAt") Instant publishedAt,
            @JsonProperty("temperature") double temperature,
            @JsonProperty("relativeHumidity") double relativeHumidity,
            @JsonProperty("precipitation") double precipitation,
            @JsonProperty("windSpeed") double windSpeed,
            @JsonProperty("dataSource") String dataSource,
            @JsonProperty("sourceReference") String sourceReference,
            @JsonProperty("sourceLatitude") double sourceLatitude,
            @JsonProperty("sourceLongitude") double sourceLongitude) {
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

    public static WeatherEvent from(District district, Observation observation,
            String sourceReference, HistoricalData source) {
        String sensorId = district.getSensorId();
        return new WeatherEvent(
                createId(sensorId, observation.getTime(), sourceReference),
                sensorId,
                district.getName(),
                district.getLatitude(),
                district.getLongitude(),
                observation.getTime(),
                Instant.now(),
                observation.getTemperature(),
                observation.getRelativeHumidity(),
                observation.getPrecipitation(),
                observation.getWindSpeed(),
                "Open-Meteo/ERA5",
                sourceReference,
                source.getLatitude(),
                source.getLongitude());
    }

    public static UUID createId(String sensorId, Instant observedAt, String sourceReference) {
        String value = sensorId + "|" + observedAt + "|" + sourceReference;
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    public UUID getEventId() { return eventId; }
    public String getSensorId() { return sensorId; }
    public String getDistrict() { return district; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Instant getObservedAt() { return observedAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public double getTemperature() { return temperature; }
    public double getRelativeHumidity() { return relativeHumidity; }
    public double getPrecipitation() { return precipitation; }
    public double getWindSpeed() { return windSpeed; }
    public String getDataSource() { return dataSource; }
    public String getSourceReference() { return sourceReference; }
    public double getSourceLatitude() { return sourceLatitude; }
    public double getSourceLongitude() { return sourceLongitude; }
}
