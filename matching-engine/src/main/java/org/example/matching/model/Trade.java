package org.example.matching.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Trade {
    String buyOrderId;
    String sellOrderId;
    long price;
   long quantity;
    long timestamp;

}