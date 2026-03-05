package org.example.matching.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class Trade {
    String buyOrderId;
    String sellOrderId;
    long price;
   long quantity;
    long timestamp;

}