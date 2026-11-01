package it.romesensors.producer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HistoricalData {
    private final double latitude;
    private final double longitude;
    private final List<Observation> observations;

    public HistoricalData(double latitude, double longitude, List<Observation> observations) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.observations = Collections.unmodifiableList(new ArrayList<Observation>(observations));
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Observation> getObservations() {
        return observations;
    }
}
