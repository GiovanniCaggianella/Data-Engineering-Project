CREATE TABLE sensors (
    sensor_id varchar(16) PRIMARY KEY,
    district varchar(40) NOT NULL,
    latitude double precision NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude double precision NOT NULL CHECK (longitude BETWEEN -180 AND 180)
);

INSERT INTO sensors VALUES
('TR-01', 'Trastevere', 41.8898, 12.4707),
('PR-01', 'Prati', 41.9097, 12.4597),
('EU-01', 'EUR', 41.8336, 12.4685),
('SG-01', 'San Giovanni', 41.8859, 12.5096),
('TE-01', 'Termini', 41.9009, 12.5018);

CREATE TABLE measurements (
    event_id uuid PRIMARY KEY,
    sensor_id varchar(16) NOT NULL REFERENCES sensors(sensor_id),
    observed_at timestamptz NOT NULL,
    published_at timestamptz NOT NULL,
    received_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    temperature double precision NOT NULL CHECK (temperature BETWEEN -90 AND 60),
    relative_humidity double precision NOT NULL CHECK (relative_humidity BETWEEN 0 AND 100),
    precipitation double precision NOT NULL CHECK (precipitation BETWEEN 0 AND 500),
    wind_speed double precision NOT NULL CHECK (wind_speed BETWEEN 0 AND 400),
    data_source text NOT NULL,
    source_reference text NOT NULL,
    source_latitude double precision NOT NULL CHECK (source_latitude BETWEEN -90 AND 90),
    source_longitude double precision NOT NULL CHECK (source_longitude BETWEEN -180 AND 180),
    temperature_alert boolean NOT NULL,
    wind_alert boolean NOT NULL,
    precipitation_alert boolean NOT NULL,
    UNIQUE (sensor_id, observed_at)
);

CREATE INDEX measurements_observed_at_idx ON measurements(observed_at);