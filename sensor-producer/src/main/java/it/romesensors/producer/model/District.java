package it.romesensors.producer.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class District {
    public static final List<District> ALL = Collections.unmodifiableList(Arrays.asList(
            new District("TR", "Trastevere", 41.8898, 12.4707),
            new District("PR", "Prati", 41.9097, 12.4597),
            new District("EU", "EUR", 41.8336, 12.4685),
            new District("SG", "San Giovanni", 41.8859, 12.5096),
            new District("TE", "Termini", 41.9009, 12.5018)));

    private final String code;
    private final String name;
    private final double latitude;
    private final double longitude;

    public District(String code, String name, double latitude, double longitude) {
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSensorId() {
        return code + "-01";
    }
}
