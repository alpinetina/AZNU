package com.vet.vet_scheduler_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppointmentController {

    @Autowired
    private ProducerTemplate producerTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/api/book")
    public String bookVisit(@RequestBody AppointmentRequest request) throws JsonProcessingException {

        // 1. Zamieniamy obiekt Javy na JSON (napis)
        String jsonMessage = objectMapper.writeValueAsString(request);

        // 2. Wrzucamy wiadomość bezpośrednio na temat Kafki
        producerTemplate.sendBody("kafka:appointment.request?brokers=kafka:29092", jsonMessage);

        return "Zgłoszenie przyjęte! ID: " + request.getRequestId();
    }
}