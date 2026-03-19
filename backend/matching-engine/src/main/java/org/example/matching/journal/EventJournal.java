//import lombok.AllArgsConstructor;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.FileAlreadyExistsException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//@AllArgsConstructor
//public class EventJournal {
//    private final Path journalDir;
//    private final Path journalFile;
//
//    public EventJournal() {
//        try {
//            journalDir = Path.of("journals");
//            Files.createDirectories(journalDir);
//
//            journalFile = Path.of("engine.log");
//            try {
//                Files.createFile(journalFile);
//            } catch (FileAlreadyExistsException e) {
//                // File already exists, that's fine
//            }
//        } catch (IOException e) {
//throw new RuntimeException("failed to journal");
//        }
//
//    }
//    public synchronized void append(String event){
//        try(FileWriter writer = new FileWriter(journalFile.toFile(),true)){
//            writer.write(event);
//            writer.write(System.lineSeparator());
//            writer.flush();
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to write to journal", e);
//        }
//    }
//    }
//

package org.example.matching.journal;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class EventJournal {

    private final Path journalDir;
    private final Path journalFile;

    public EventJournal() {
        try {
            journalDir = Path.of("journals");
            Files.createDirectories(journalDir);
            journalFile = journalDir.resolve("engine.log");
            if (!Files.exists(journalFile)) {
                Files.createFile(journalFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize EventJournal", e);
        }
    }

    public synchronized void appendRaw(String eventBody) {
        try (FileWriter writer = new FileWriter(journalFile.toFile(), true)) {
            writer.write(eventBody);
            writer.write(System.lineSeparator());
            writer.flush();
            // for production consider FileDescriptor.sync to fsync()
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to journal", e);
        }
    }

    // helpers with consistent format
    public void appendOrder(String id, String user, String side, long price, int qty, long ts) {
        String line = String.format("ORDER %s %s %s %d %d %d", id, user, side, price, qty, ts);
        appendRaw(line);
    }

    public void appendTrade(String buyId, String sellId, long price, int qty, long ts) {
        String line = String.format("TRADE %s %s %d %d %d", buyId, sellId, price, qty, ts);
        appendRaw(line);
    }

    public List<String> readAllLines() {
        try {
            return Files.readAllLines(journalFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read journal", e);
        }
    }

    public Path getJournalFile() {
        return journalFile;
    }
}
