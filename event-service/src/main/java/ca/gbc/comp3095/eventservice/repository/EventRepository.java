package ca.gbc.comp3095.eventservice.repository;

import ca.gbc.comp3095.eventservice.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}