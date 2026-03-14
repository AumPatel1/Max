package org.example.matching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketEvent {
    private String id;
    private String question;
    private String yesTicker;
    private String noTicker;
    private int expiry;
    private EventStatus status;
}
