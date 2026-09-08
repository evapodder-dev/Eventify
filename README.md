# Eventify — Event Management & Coordination System

A university-level desktop application built for the **Advanced Programming Laboratory (APL)** course to manage university events (workshops, programming contests, seminars, competitions, club programs, and cultural events).

## Tech Stack
- **Language:** Java 21
- **GUI Framework:** JavaFX 21 + FXML (Pure FXML/Java styling, no external CSS)
- **Database:** SQLite + JDBC (`PreparedStatement`, DAO pattern)
- **Concurrency:** Java Multithreading (`Task`, `ExecutorService`, `Future`, `Platform.runLater`)
- **Data Exchange:** Gson (JSON Import/Export) & Java `HttpClient` (API Response Handling)
- **Build Tool:** Apache Maven

## How to Run
```bash
mvn clean javafx:run
```
