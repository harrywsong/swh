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

-- Insert seed data
INSERT INTO t_wellness_resource (title, description, category, url) VALUES
                                                                        ('Counseling Services', 'Professional mental health counseling', 'counseling', 'https://www.georgebrown.ca/counseling'),
                                                                        ('Mindfulness Guide', 'Meditation practices guide', 'mindfulness', 'https://www.georgebrown.ca/mindfulness'),
                                                                        ('Stress Management', 'Academic stress management', 'wellness', 'https://www.georgebrown.ca/stress');

INSERT INTO t_events (title, description, date, location, capacity, registered_students) VALUES
                                                                                             ('Yoga Workshop', 'Beginner yoga session', '2025-11-15 10:00:00', 'Wellness Center', 30, 0),
                                                                                             ('Mental Health Seminar', 'College mental health', '2025-11-20 14:00:00', 'Main Auditorium', 100, 0);