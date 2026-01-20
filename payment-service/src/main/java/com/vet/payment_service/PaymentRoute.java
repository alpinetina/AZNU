package com.vet.payment_service;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Random;

@Component
public class PaymentRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // 1. Słuchamy na kanale "Rezerwacja Wstępna"
        from("kafka:payment.request?brokers=kafka:29092&groupId=payment-service")
                .log(" Otrzymano zlecenie płatności: ${body}")

                // Zamieniamy JSON na Mapę (klucz-wartość), żeby łatwo odczytać dane
                .unmarshal().json(JsonLibrary.Jackson, Map.class)

                .process(exchange -> {
                    Map data = exchange.getMessage().getBody(Map.class);
                    Integer requestId = (Integer) data.get("requestId");

                    // SYMULACJA PŁATNOŚCI:
                    // Załóżmy, że ID = 13 to pechowa liczba (brak środków na koncie)
                    // Każde inne ID to sukces.
                    boolean paymentSuccess = (requestId != 13);

                    exchange.getMessage().setHeader("paymentSuccess", paymentSuccess);
                    exchange.getMessage().setHeader("requestId", requestId);
                })

                // 2. Decyzja
                .choice()
                .when(header("paymentSuccess").isEqualTo(true))
                // SUKCES
                .log(" Płatność przyjęta dla ID: ${header.requestId}. Koniec Sagi.")
                .setBody(simple("{\"status\":\"PAID\", \"requestId\":${header.requestId}}"))
                .to("kafka:booking.confirmed?brokers=kafka:29092")

                .otherwise()
                // PORAŻKA
                .log(" Płatność odrzucona (brak środków) dla ID: ${header.requestId}.")
                .setBody(simple("{\"status\":\"PAYMENT_FAILED\", \"requestId\":${header.requestId}}"))
                // To zdarzenie będzie ważne dla Schedulera, żeby cofnąć rezerwację!
                .to("kafka:payment.failed?brokers=kafka:29092")
                .end();
    }
}