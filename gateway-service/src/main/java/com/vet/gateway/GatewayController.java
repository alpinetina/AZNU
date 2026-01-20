package com.vet.gateway;

import com.fasterxml.jackson.databind.ObjectMapper; // <--- WAŻNY IMPORT
import org.apache.camel.ProducerTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final ProducerTemplate producerTemplate;
    private final BookingStorage bookingStorage;
    private final ObjectMapper objectMapper = new ObjectMapper(); // <--- To nasze narzędzie do JSON

    public GatewayController(ProducerTemplate producerTemplate, BookingStorage bookingStorage) {
        this.producerTemplate = producerTemplate;
        this.bookingStorage = bookingStorage;
    }

    @PostMapping("/book")
    public String bookVisit(@RequestBody Map<String, Object> request) {
        Integer requestId = (Integer) request.get("requestId");

        try {
            // 1. Zapisujemy status
            bookingStorage.saveStatus(requestId, "PROCESSING");

            // 2. ZAMIENIAMY MAPĘ NA PRAWDZIWY JSON (STRING)
            String jsonMessage = objectMapper.writeValueAsString(request);

            // 3. Wysyłamy JSON do Kafki
            producerTemplate.sendBody("kafka:appointment.request?brokers=kafka:29092", jsonMessage);

            return "Przyjęto zgłoszenie nr " + requestId + ". Sprawdź status pod /api/status/" + requestId;

        } catch (Exception e) {
            return "Błąd przetwarzania: " + e.getMessage();
        }
    }

    @GetMapping("/status/{id}")
    public String checkStatus(@PathVariable Integer id) {
        return bookingStorage.getStatus(id);
    }
}