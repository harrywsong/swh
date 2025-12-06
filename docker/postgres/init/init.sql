-- Create keycloak database
CREATE DATABASE keycloak;

-- Create wellness database tables
\c wellness_db;

CREATE TABLE IF NOT EXISTS t_wellness_resource (
    resource_id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    category VARCHAR(100),
    url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS t_events (
    event_id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    description TEXT,
    date TIMESTAMP,
    location VARCHAR(255),
    capacity INTEGER,
    registered_students INTEGER
);

-- New table for tracking resource popularity
CREATE TABLE IF NOT EXISTS t_resource_popularity (
    id SERIAL PRIMARY KEY,
    category VARCHAR(100) UNIQUE,
    view_count INTEGER DEFAULT 0,
    goal_completion_count INTEGER DEFAULT 0,
    last_updated TIMESTAMP
);
