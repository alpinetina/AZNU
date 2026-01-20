package com.vet.vet_scheduler_service;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class SchedulerRoute extends RouteBuilder {

    private final SoapClient soapClient;

    public SchedulerRoute(SoapClient soapClient) {
        this.soapClient = soapClient;
    }

    @Override
    public void configure() throws Exception {

        // 1. Słuchamy na temacie "appointment.request" w Kafce
        from("kafka:appointment.request?brokers=kafka:29092&groupId=vet-scheduler-NOWA")
                .log("📥 Otrzymano zgłoszenie: ${body}")

                // 2. Zamieniamy JSON-a (napis) na obiekt Java (AppointmentRequest)
                .unmarshal().json(JsonLibrary.Jackson, AppointmentRequest.class)

                // 3. Logika biznesowa: Dzwonimy do SOAP
                .process(exchange -> {
                    // Wyciągamy dane z wiadomości
                    AppointmentRequest req = exchange.getMessage().getBody(AppointmentRequest.class);

                    // Dzwonimy do starego systemu
                    boolean isAvailable = soapClient.checkAvailability(
                            req.getOwner(),
                            req.getPetName(),
                            req.getVisitDate()
                    );

                    // Zapisujemy wynik w nagłówku wiadomości (żeby użyć go za chwilę w "choice")
                    exchange.getMessage().setHeader("isVetAvailable", isAvailable);

                    // Zachowujemy też ID zgłoszenia, żeby wiedzieć, co potwierdzamy
                    exchange.getMessage().setHeader("requestId", req.getRequestId());
                })

                // 4. Decyzja (Router): Czy weterynarz jest dostępny?
                .choice()
                .when(header("isVetAvailable").isEqualTo(true))
                // A) Jeśli TAK:
                .log(" Weterynarz dostępny! Rezerwuję termin.")
                // Tworzymy nową wiadomość JSON: { "status": "CONFIRMED", "id": ... }
                .setBody(simple("{\"status\":\"CONFIRMED\", \"requestId\":${header.requestId}}"))
                // Wysyłamy na kolejny temat (dla Płatności)
                .to("kafka:payment.request?brokers=kafka:29092")

                .otherwise()
                // B) Jeśli NIE:
                .log(" Brak terminów (SOAP odrzucił).")
                .setBody(simple("{\"status\":\"FAILED\", \"requestId\":${header.requestId}}"))
                // Wysyłamy na temat błędów
                .to("kafka:booking.failed?brokers=kafka:29092")
                .end();

        // --- TRASA KOMPENSACYJNA (SAGA) ---
        // Słuchamy, czy płatność się nie udała
        from("kafka:payment.failed?brokers=kafka:29092&groupId=vet-scheduler-undo")
                .log("🚨 Otrzymano info o błędzie płatności dla ID: ${body}")

                // Parsujemy JSON
                .unmarshal().json(JsonLibrary.Jackson, java.util.Map.class)

                .process(exchange -> {
                    java.util.Map data = exchange.getMessage().getBody(java.util.Map.class);
                    // W prawdziwym systemie ID rezerwacji SOAP mielibyśmy w bazie danych.
                    // Tutaj dla uproszczenia założymy, że ID rezerwacji to "SLOT-" + requestId
                    String bookingIdToCancel = "SLOT-" + data.get("requestId");

                    // Dzwonimy do SOAP, żeby cofnąć
                    soapClient.cancelBooking(bookingIdToCancel);
                })
                .log(" Rezerwacja cofnięta pomyślnie. System spójny.");
    }
}
