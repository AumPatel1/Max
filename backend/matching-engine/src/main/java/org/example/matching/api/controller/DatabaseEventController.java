package org.example.matching.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.matching.api.service.DatabaseEventService;
import org.example.matching.entity.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/db/events")
@RequiredArgsConstructor
public class DatabaseEventController {

    private final DatabaseEventService eventService;

    @PostMapping("/create")
    public ResponseEntity<String> createEvent(@RequestBody Map<String, Object> request) {
        try {
            Event event = eventService.createEvent(
                (String) request.get("title"),
                (String) request.get("description"),
                (String) request.get("category"),
                LocalDateTime.parse((String) request.get("closesAt"))
            );
            return ResponseEntity.ok("Event created with ID: " + event.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Event creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/open")
    public ResponseEntity<List<Event>> getOpenEvents() {
        List<Event> events = eventService.findAllOpenEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEvent(@PathVariable Long eventId) {
        return eventService.findById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{eventId}/settle")
    public ResponseEntity<String> settleEvent(@PathVariable Long eventId, @RequestBody Map<String, String> request) {
        try {
            String outcome = request.get("outcome");
            Event.EventStatus status = "YES".equals(outcome) ? 
                Event.EventStatus.RESOLVED : Event.EventStatus.CANCELLED;
            
            Event event = eventService.settleEvent(eventId, status);
            return ResponseEntity.ok("Event settled: " + event.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Settlement failed: " + e.getMessage());
        }
    }
}
