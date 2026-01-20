package com.vet.gateway;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ResultListenerRoute extends RouteBuilder {

    private final BookingStorage bookingStorage;

    public ResultListenerRoute(BookingStorage bookingStorage) {
        this.bookingStorage = bookingStorage;
    }

    @Override
    public void configure() throws Exception {
        // Nasłuchujemy SUKCESÓW (z Payment Service)
        from("kafka:booking.confirmed?brokers=kafka:29092&groupId=gateway-group")
                .unmarshal().json(JsonLibrary.Jackson, Map.class)
                .process(exchange -> {
                    Map data = exchange.getMessage().getBody(Map.class);
                    Integer id = (Integer) data.get("requestId");
                    bookingStorage.saveStatus(id, "CONFIRMED_AND_PAID");
                    log.info("✅ Zaktualizowano status dla ID " + id + ": CONFIRMED");
                });

        // Nasłuchujemy PORAŻEK (z Payment Service - Saga)
        from("kafka:payment.failed?brokers=kafka:29092&groupId=gateway-group")
                .unmarshal().json(JsonLibrary.Jackson, Map.class)
                .process(exchange -> {
                    Map data = exchange.getMessage().getBody(Map.class);
                    Integer id = (Integer) data.get("requestId");
                    bookingStorage.saveStatus(id, "FAILED_PAYMENT_REJECTED");
                    log.info("❌ Zaktualizowano status dla ID " + id + ": FAILED");
                });
    }
}
