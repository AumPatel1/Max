//package org.example.matching;
//
//import org.example.matching.matching.MatchingEngine;
//import org.example.matching.model.Order;
//import org.example.matching.model.OrderBook;
//import org.example.matching.model.OrderSide;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.Scanner;
//
//public class Replay {
//   // MatchingEngine matchingEngine = new MatchingEngine();
//
//    public void replayJournal(String filePath, OrderBook book) {
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] tokens = line.trim().split("\\s+");
//
//                if (tokens.length >= 7 && tokens[0].equals("ORDER")) {
//                    try {
//                        String id = tokens[1];
//                        String userId = tokens[2];
//
//                        // FIXED INDICES BASED ON YOUR LOG:
//                        long price = Long.parseLong(tokens[3]);     // Was [4]
//                        int quantity = Integer.parseInt(tokens[4]); // Was [5]
//                        long timestamp = Long.parseLong(tokens[5]);
//                        OrderSide side = OrderSide.valueOf(tokens[6].toUpperCase());
//
//                        Order reOrder = new Order(id, userId, price, quantity, timestamp, side);
//
//                        // Let's add a print here so you can SEE it working
//                        System.out.println("Replaying: " + side + " " + quantity + " @ " + price);
//
//                        book.placeOrder(reOrder);
//
//                    } catch (Exception e) {
//                        System.err.println("Error parsing line: " + line + " | " + e.getMessage());
//                    }
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("File not found: " + e.getMessage());
//        }
//    }}




package org.example.matching;

import org.example.matching.matching.MatchingEngine;
import org.example.matching.model.Order;
import org.example.matching.model.OrderSide;
import org.example.matching.journal.EventJournal;

import java.util.List;

public class Replay {
    private final MatchingEngine engine;
    private final EventJournal journal;

    public Replay(MatchingEngine engine) {
        this.engine = engine;
        this.journal = new EventJournal();
    }

    public void replayJournal() {
        List<String> lines = journal.readAllLines();
        for (String raw : lines) {
            if (raw == null || raw.isBlank()) continue;
            String[] first = raw.split("\\s+");
            if (first.length < 2) continue;

            if ("ORDER".equals(first[0]) && first.length >= 7) {
                try {
                    String id = first[1];
                    String user = first[2];
                    String sideToken = first[3];
                    long price = Long.parseLong(first[4]);
                    int qty = Integer.parseInt(first[5]);
                    long ts = Long.parseLong(first[6]);
                    OrderSide side = OrderSide.valueOf(sideToken);
                    Order o = new Order(id, user, price, qty, ts, side, "DEFAULT"); // Use DEFAULT instrument for replay
                    System.out.println("Replaying: " + side + " " + qty + " @ " + price);
                    engine.replayOrder(o); // replay mode (no journaling)
                } catch (Exception e) {
                    System.err.println("Skipping malformed ORDER line: " + raw + " -> " + e.getMessage());
                }
            }
            // ignore TRADE lines
        }
    }
}
