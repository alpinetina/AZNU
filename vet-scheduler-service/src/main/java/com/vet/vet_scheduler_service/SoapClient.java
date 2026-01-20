package com.vet.vet_scheduler_service;

import com.vet.scheduler.wsdl.GetBookSlotRequest;
import com.vet.scheduler.wsdl.GetBookSlotResponse;
import com.vet.scheduler.wsdl.CancelBookingRequest;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

public class SoapClient extends WebServiceGatewaySupport {

    public boolean checkAvailability(String owner, String pet, String date) {
        // 1. Tworzymy zapytanie SOAP (używając klas wygenerowanych z XSD)
        GetBookSlotRequest request = new GetBookSlotRequest();
        request.setOwner(owner);
        request.setPetName(pet);
        request.setDate(date);
        request.setDoctor("Dr House");

        // 2. Wysyłamy i odbieramy odpowiedź
        GetBookSlotResponse response = (GetBookSlotResponse) getWebServiceTemplate()
                .marshalSendAndReceive("http://legacy-vet-system:8080/ws/bookings", request);

        // 3. Zwracamy wynik (true/false)
        return response.isIsAvailable();
    }

    public void cancelBooking(String bookingId) {
        com.vet.scheduler.wsdl.CancelBookingRequest request = new com.vet.scheduler.wsdl.CancelBookingRequest();
        request.setBookingId(bookingId);

        getWebServiceTemplate()
                .marshalSendAndReceive("http://legacy-vet-system:8080/ws/bookings", request);

        System.out.println("📞 Wysłano żądanie kompensacji (Anuluj) do SOAP.");
    }
}