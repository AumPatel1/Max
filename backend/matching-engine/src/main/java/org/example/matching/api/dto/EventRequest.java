package org.example.matching.api.dto;

import lombok.Data;

@Data
public class EventRequest {
    private String id;
    private String questions; // Matches your JSON "questions"
    private String yesTicker;
    private String noTicker;
    private int expiry;
}