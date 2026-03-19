package org.example.matching.validation;

import org.example.matching.model.Order;

public class OrderValidator {
    public boolean validate(Order order) {

        if (order == null) {
            return false;
        }

        if (order.getId() == null || order.getId().isBlank()) {
            return false;
        }

        if (order.getUserId() == null || order.getUserId().isBlank()) {
            return false;
        }

        if (order.getSide() == null) {
            return false;
        }

        if (order.getPrice() <= 0) {
            return false;
        }

        if (order.getQuantity() <= 0) {
            return false;
        }

        return true;
    }
}