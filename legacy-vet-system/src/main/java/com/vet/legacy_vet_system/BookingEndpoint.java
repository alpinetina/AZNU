package com.vet.legacy_vet_system; // Upewnij się, że pakiet jest zgodny z Twoim projektem

import com.vet.legacy_vet_system.GetBookSlotRequest;
import com.vet.legacy_vet_system.GetBookSlotResponse;
import com.vet.legacy_vet_system.CancelBookingRequest;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.UUID;

@Endpoint
public class BookingEndpoint {

    // To musi być DOKŁADNIE to samo co w pliku bookings.xsd (targetNamespace)
    private static final String NAMESPACE_URI = "http://vet.com/legacy-vet-system";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBookSlotRequest")
    @ResponsePayload
    public GetBookSlotResponse getBookSlot(@RequestPayload GetBookSlotRequest request) {
        GetBookSlotResponse response = new GetBookSlotResponse();

        // Prosta logika:
        // Jeśli właścicielem jest "Janusz", odrzucamy (żebyś mógł potem przetestować błędy)
        // Każdy inny jest akceptowany.

        if ("Janusz".equalsIgnoreCase(request.getOwner())) {
            response.setIsAvailable(false);
            response.setConfirmationMessage("Brak terminów dla tego klienta.");
            response.setBookingId("");
        } else {
            response.setIsAvailable(true);
            response.setConfirmationMessage("Wizyta potwierdzona dla: " + request.getPetName());
            response.setBookingId(UUID.randomUUID().toString()); // Generujemy losowe ID
        }

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "cancelBookingRequest")
    @ResponsePayload
    public CancelBookingResponse cancelBooking(@RequestPayload CancelBookingRequest request) {
        CancelBookingResponse response = new CancelBookingResponse();
        // Symulujemy anulowanie
        System.out.println(" ANULOWANIE REZERWACJI ID: " + request.getBookingId());
        response.setStatus("CANCELLED");
        return response;
    }
}