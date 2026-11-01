package it.romesensors.producer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.romesensors.producer.model.District;
import it.romesensors.producer.model.HistoricalData;
import it.romesensors.producer.model.Observation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeatherSource {
    private static final Logger LOG = LoggerFactory.getLogger(WeatherSource.class);
    private static final List<String> VARIABLES = Arrays.asList(
            "temperature_2m", "relative_humidity_2m", "precipitation", "wind_speed_10m");

    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Path dataDirectory;

    public WeatherSource(
            ObjectMapper objectMapper,
            @Value("${app.source-url}") String baseUrl,
            @Value("${app.start-date}") LocalDate startDate,
            @Value("${app.end-date}") LocalDate endDate,
            @Value("${app.data-dir}") Path dataDirectory) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must not precede start date");
        }
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dataDirectory = dataDirectory;
    }

    public HistoricalData load(District district) throws IOException {
        Path file = dataDirectory.resolve(
                district.getCode() + "-" + startDate + "-" + endDate + ".json");
        LOG.info("Loading historical data from {}", file);
        return parse(Files.readString(file));
    }

    public String getReference(District district) {
        return baseUrl
                + "?latitude=" + district.getLatitude()
                + "&longitude=" + district.getLongitude()
                + "&start_date=" + startDate.minusDays(1)
                + "&end_date=" + endDate
                + "&hourly=temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m"
                + "&models=era5&timezone=GMT&timeformat=unixtime"
                + "&temperature_unit=celsius&wind_speed_unit=kmh&precipitation_unit=mm";
    }

    HistoricalData parse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        if (root == null || root.path("error").asBoolean(false)
                || !root.path("latitude").isNumber() || !root.path("longitude").isNumber()) {
            throw new IOException("Invalid Open-Meteo response");
        }

        JsonNode hourly = root.path("hourly");
        JsonNode times = hourly.path("time");
        if (!times.isArray() || times.size() == 0) {
            throw new IOException("Missing hourly data");
        }
        for (String variable : VARIABLES) {
            if (!hourly.path(variable).isArray() || hourly.path(variable).size() != times.size()) {
                throw new IOException("Invalid hourly data: " + variable);
            }
        }

        ZoneId rome = ZoneId.of("Europe/Rome");
        Instant start = startDate.atStartOfDay(rome).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(rome).toInstant();
        List<Observation> observations = new ArrayList<Observation>();

        for (int index = 0; index < times.size(); index++) {
            if (!times.get(index).isIntegralNumber() || !hasValues(hourly, index)) {
                continue;
            }
            Instant time = Instant.ofEpochSecond(times.get(index).asLong());
            if (!time.isBefore(start) && time.isBefore(end)) {
                observations.add(new Observation(
                        time,
                        hourly.path(VARIABLES.get(0)).get(index).asDouble(),
                        hourly.path(VARIABLES.get(1)).get(index).asDouble(),
                        hourly.path(VARIABLES.get(2)).get(index).asDouble(),
                        hourly.path(VARIABLES.get(3)).get(index).asDouble()));
            }
        }

        if (observations.isEmpty()) {
            throw new IOException("No usable observations");
        }
        return new HistoricalData(
                root.path("latitude").asDouble(), root.path("longitude").asDouble(), observations);
    }

    private boolean hasValues(JsonNode hourly, int index) {
        for (String variable : VARIABLES) {
            JsonNode value = hourly.path(variable).get(index);
            if (!value.isNumber() || !Double.isFinite(value.asDouble())) {
                return false;
            }
        }
        return true;
    }

}
