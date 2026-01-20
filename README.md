# System Rezerwacji Weterynaryjnych (Microservices Architecture)

Projekt zaliczeniowy implementujący architekturę mikroserwisów z wykorzystaniem Spring Boot, Apache Kafka, SOAP oraz Docker.

## 🚀 Technologie
* **Java 17** (Spring Boot 3)
* **Apache Kafka** (Message Broker)
* **Docker & Docker Compose** (Konteneryzacja)
* **SOAP Web Services** (Integracja z Legacy System)
* **REST API + Frontend** (Gateway & GUI)

## 🏗 Architektura
System składa się z następujących kontenerów:

1.  **Gateway Service (Port 8083):** API REST oraz Web GUI. Punkt wejścia dla klienta.
2.  **Vet Scheduler Service:** Logika rezerwacji, komunikacja z Legacy System (SOAP).
3.  **Payment Service:** Obsługa płatności.
4.  **Legacy Vet System:** Symulator zewnętrznego systemu gabinetu (SOAP).
5.  **Kafka & Zookeeper:** Szyna komunikacyjna.

## 🔄 Wzorzec SAGA (Spójność Danych)
Zastosowano Sagę opartą na choreografii:
1.  Rezerwacja wstępna w Legacy System (SOAP).
2.  Próba płatności.
3.  **Sukces:** Potwierdzenie rezerwacji.
4.  **Błąd Płatności:** Scheduler otrzymuje zdarzenie `payment.failed` i wykonuje **transakcję kompensacyjną** (anuluje wizytę w Legacy System).

## 🛠 Jak uruchomić?

Wymagany zainstalowany Docker Desktop.

1.  W terminalu w głównym katalogu projektu zbuduj aplikacje (opcjonalnie, jeśli nie ma plików .jar):
    *(Wymaga Maven Wrapper lub Maven)*
    ```bash
    # Windows
    cd gateway-service; .\mvnw clean package -DskipTests; cd ..
    cd payment-service; .\mvnw clean package -DskipTests; cd ..
    cd vet-scheduler-service; .\mvnw clean package -DskipTests; cd ..
    cd legacy-vet-system; .\mvnw clean package -DskipTests; cd ..
    ```

2.  Uruchom środowisko Dockerowe:
    ```bash
    docker-compose up -d --build
    ```

3.  Poczekaj ok. 1-2 minuty na start wszystkich serwisów.

## 🖥 Obsługa

1.  Otwórz przeglądarkę: **http://localhost:8083**
2.  **Scenariusz Pozytywny:** Wpisz dowolne ID (np. `100`). Status zmieni się na `CONFIRMED_AND_PAID`.
3.  **Scenariusz SAGA (Błąd):** Wpisz ID `13`. Płatność zostanie odrzucona, a system cofnie rezerwację (Status: `FAILED`, Logi: `Rezerwacja cofnięta`).

## 📚 Dokumentacja API
Swagger UI dostępny jest pod adresem:
http://localhost:8083/swagger-ui.html