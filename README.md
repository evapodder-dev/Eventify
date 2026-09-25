# Eventify — Event Management & Coordination System

A complete university-level JavaFX desktop application built for the **Advanced Programming Laboratory (APL)** course.

## 1. Project Overview
**Eventify** allows university organizers and participants to manage:
- **Workshops, Programming Contests, Seminars, Competitions, Club Programs & Cultural Events**
- **Participant Registrations & Attendance Marking**
- **Event Activity Schedules**
- **Preparation Tasks & Priorities**
- **Competition Leaderboards & Automated Ranking**
- **JSON Import/Export & Live HTTP REST API Event Integration**

---

## 2. APL Topic Coverage

| APL Topic | Eventify Implementation |
| :--- | :--- |
| **Java Syntax & OOP** | Encapsulation, Inheritance (`User` -> `Organizer`, `Participant`), Polymorphism (`getAccessSummary()`, `canManageSystem()`), Generic Interface (`CrudDAO<T, ID>`), `Comparable<Result>`, Custom Exception (`DatabaseException`) |
| **JavaFX Desktop GUI & FXML** | 8 pure FXML screens (`login`, `dashboard`, `events`, `participants`, `schedule`, `tasks`, `leaderboard`, `api-events`) styled strictly through FXML properties without external CSS files |
| **SQLite + JDBC** | 8 relational tables (`users`, `events`, `participants`, `registrations`, `attendance`, `schedules`, `tasks`, `results`) with Primary Keys, Foreign Keys, constraints, and `PreparedStatement` DAOs |
| **Multithreading & Concurrency** | `AppExecutor` fixed daemon thread pool (`ExecutorService`), `DashboardService` parallel `Future<T>` queries, and JavaFX `Task<T>` callbacks so SQLite/HTTP/JSON operations never block the JavaFX Application Thread |
| **JSON Parsing** | `JsonUtil` using Google Gson to import and export `Event` objects to/from `.json` files |
| **API Response Handling** | `ApiEventService` using `HttpClient`, `HttpRequest`, and `HttpResponse` with timeout, HTTP status, network, and invalid JSON handling plus local JSON fallback |

---

## 3. Default Login Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| **Organizer** | `admin` | `admin123` |
| **Participant** | `student` | `student123` |

---

## 4. How to Run

### Using Maven
```bash
mvn clean javafx:run
```

### In IntelliJ IDEA
Open the **Maven** tool window -> **Eventify** -> **Plugins** -> **javafx** -> double-click **`javafx:run`**.
