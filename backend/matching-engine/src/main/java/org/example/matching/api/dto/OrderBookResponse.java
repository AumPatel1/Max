package org.example.matching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderBookResponse {
    private String instrument;
    private List<PriceLevel> bids;
    private List<PriceLevel> asks;
}
