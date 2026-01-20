package com.vet.gateway;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BookingStorage {
    // Mapa: ID Zamówienia -> Status (np. "PROCESSING", "CONFIRMED", "FAILED")
    private final ConcurrentHashMap<Integer, String> storage = new ConcurrentHashMap<>();

    public void saveStatus(Integer id, String status) {
        storage.put(id, status);
    }

    public String getStatus(Integer id) {
        return storage.getOrDefault(id, "UNKNOWN_ID");
    }
}