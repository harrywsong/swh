-- PostgreSQL initialization script
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

-- Seed data removed for testing - tests will create their own data