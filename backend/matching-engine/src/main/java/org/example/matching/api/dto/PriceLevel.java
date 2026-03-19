package org.example.matching.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceLevel {
    private Long price;
    private Long quantity;
}
