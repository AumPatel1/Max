package org.example.matching.api.service;

import lombok.RequiredArgsConstructor;
import org.example.matching.entity.Event;
import org.example.matching.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DatabaseEventService {

    private final EventRepository eventRepository;

    public Event createEvent(String title, String description, String category, LocalDateTime closesAt) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setCategory(category);
        event.setStatus(Event.EventStatus.OPEN);
        event.setClosesAt(closesAt);
        
        return eventRepository.save(event);
    }

    public Optional<Event> findById(Long eventId) {
        return eventRepository.findById(eventId);
    }

    public List<Event> findAllOpenEvents() {
        return eventRepository.findByStatus(Event.EventStatus.OPEN);
    }

    public Event settleEvent(Long eventId, Event.EventStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        
        event.setStatus(status);
        event.setResolvesAt(LocalDateTime.now());
        
        return eventRepository.save(event);
    }
}
