package it.romesensors.producer.model;

import java.time.Instant;

public final class Observation {
    private final Instant time;
    private final double temperature;
    private final double relativeHumidity;
    private final double precipitation;
    private final double windSpeed;

    public Observation(Instant time, double temperature, double relativeHumidity,
            double precipitation, double windSpeed) {
        this.time = time;
        this.temperature = temperature;
        this.relativeHumidity = relativeHumidity;
        this.precipitation = precipitation;
        this.windSpeed = windSpeed;
    }

    public Instant getTime() {
        return time;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getRelativeHumidity() {
        return relativeHumidity;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}
