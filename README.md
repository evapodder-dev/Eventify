# Eventify — University Event Management & Coordination System

A full-featured JavaFX desktop application built for the **Advanced Programming Laboratory (APL)** course, featuring role-based access control (**Organizer / Admin** vs. **Participant**), an 8-table **SQLite + JDBC** relational database, **multithreaded concurrency** (`ExecutorService`, `Future<T>`, JavaFX `Task<T>`), **Jackson JSON** import/export, and live **Java `HttpClient` REST API** integration.

---

## 1. Key Features & Representative Code Links

### 1.1 Authentication, Sign Up & Role-Based Session Management
- **Sign In & Instant Sign Up by Roll / User ID**: Supports both logging in with existing credentials and creating new Participant/Organizer accounts directly from the login screen, automatically provisioning a matching participant profile for new student Roll numbers.
  - **UI Layout**: [`login.fxml`](src/main/resources/fxml/login.fxml)
  - **Controller Logic**: [`LoginController.handleLogin()`](src/main/java/com/eventify/controller/LoginController.java#L65-L116) & [`LoginController.handleSignUp()`](src/main/java/com/eventify/controller/LoginController.java#L119-L159)
  - **Database Persistence**: [`UserDAO.authenticate()`](src/main/java/com/eventify/dao/UserDAO.java#L25-L44) & [`UserDAO.registerUser()`](src/main/java/com/eventify/dao/UserDAO.java#L50-L120)
- **Strict Role Separation (Admin `EVA PODDER` vs. Participant Roll)**:
  - **Admin (`admin`)**: Full control over Event CRUD, Participants & Attendance, Schedule CRUD, Organizer Tasks, and Leaderboard Score/Rank management.
  - **Participant (e.g., Roll `2307032`)**: Customized student view with open/closed event status indicators, 1-click event registration, personal registration tracking, and read-only Schedule & Leaderboard views.
  - **Session & Role Guards**: [`SessionManager.java`](src/main/java/com/eventify/util/SessionManager.java#L10-L43), [`DashboardController.applyRolePermissions()`](src/main/java/com/eventify/controller/DashboardController.java#L113-L163), [`EventController.applyRolePermissions()`](src/main/java/com/eventify/controller/EventController.java#L171-L225)

---

### 1.2 Interactive Dashboard & Concurrent Statistics
- **Parallel Statistics Computation**: Executes 6 concurrent SQLite queries (`totalEvents`, `upcomingEvents`, `ongoingEvents`, `totalParticipants`, `pendingTasks`, `recentEvents`) in parallel using `ExecutorService` and `Future<T>`.
  - **Concurrency Service**: [`DashboardService.loadDashboardSummaryConcurrently()`](src/main/java/com/eventify/service/DashboardService.java#L36-L61)
  - **Dashboard Controller & Quick Actions**: [`DashboardController.loadDashboardStatistics()`](src/main/java/com/eventify/controller/DashboardController.java#L166-L209)
  - **UI Layout**: [`dashboard.fxml`](src/main/resources/fxml/dashboard.fxml)

---

### 1.3 University Event Management & 1-Click Participant Registration
- **Admin Event CRUD & Search/Filter**: Create, update, delete, view details, and filter university events (`Workshop`, `Programming Contest`, `Seminar`, `Competition`, `Club Program`, `Cultural Event`) by keyword and status (`Upcoming`, `Ongoing`, `Completed`).
  - **Controller CRUD Handlers**: [`EventController.handleAddEvent()`](src/main/java/com/eventify/controller/EventController.java#L275-L298), [`handleUpdateEvent()`](src/main/java/com/eventify/controller/EventController.java#L301-L328), [`handleDeleteEvent()`](src/main/java/com/eventify/controller/EventController.java#L331-L354), [`handleSearch()`](src/main/java/com/eventify/controller/EventController.java#L255-L272)
  - **Service & Validation Layer**: [`EventService.java`](src/main/java/com/eventify/service/EventService.java#L17-L95)
  - **DAO Layer**: [`EventDAO.java`](src/main/java/com/eventify/dao/EventDAO.java#L20-L185)
- **Participant 1-Click Event Registration & Status Tracking**: Participants see whether each event is `Upcoming (Open)`, `Ongoing (Open)`, or `Completed (Closed)`, can register with one click using their signed-in Roll, and can inspect their registered events and attendance status.
  - **Registration Handlers**: [`EventController.handleRegisterSelectedEvent()`](src/main/java/com/eventify/controller/EventController.java#L357-L393) & [`EventController.handleViewMyRegistrations()`](src/main/java/com/eventify/controller/EventController.java#L396-L428)
  - **Roll-Based Registration DAO**: [`RegistrationDAO.registerByRoll()`](src/main/java/com/eventify/dao/RegistrationDAO.java#L65-L128) & [`RegistrationDAO.findByRoll()`](src/main/java/com/eventify/dao/RegistrationDAO.java#L166-L195)
  - **UI Layout**: [`events.fxml`](src/main/resources/fxml/events.fxml)

---

### 1.4 Participants & Attendance Control (Admin-Only)
- **Student Profile Management (Roll-Based)**: Add, update, delete, search, and view participant profiles identified by university **Roll** (e.g., `2307032`), department, academic year, email, and phone.
  - **Participant Controller**: [`ParticipantController.java`](src/main/java/com/eventify/controller/ParticipantController.java#L165-L280)
  - **Participant DAO**: [`ParticipantDAO.java`](src/main/java/com/eventify/dao/ParticipantDAO.java#L19-L157)
- **Event Registration & Live Attendance Marking**: Assign participants to events and mark/update attendance status (`Present`, `Absent`, `Not Marked`) synchronized across both `registrations` and `attendance` tables.
  - **Attendance & Registration Handlers**: [`ParticipantController.handleRegisterParticipant()`](src/main/java/com/eventify/controller/ParticipantController.java#L283-L315) & [`ParticipantController.handleUpdateAttendance()`](src/main/java/com/eventify/controller/ParticipantController.java#L318-L353)
  - **Attendance DAO Logic**: [`RegistrationDAO.updateAttendance()`](src/main/java/com/eventify/dao/RegistrationDAO.java#L197-L234)
  - **UI Layout**: [`participants.fxml`](src/main/resources/fxml/participants.fxml)

---

### 1.5 Event Activity Schedule Management
- **Time-Slot & Activity Coordination**: Manage event sessions, activity names, dates, start/end times, and venues with event-based filtering. Read-only for Participants; full CRUD for Admin.
  - **Schedule Controller**: [`ScheduleController.java`](src/main/java/com/eventify/controller/ScheduleController.java#L30-L355)
  - **Schedule DAO**: [`ScheduleDAO.java`](src/main/java/com/eventify/dao/ScheduleDAO.java#L19-L139)
  - **Schedule Model**: [`Schedule.java`](src/main/java/com/eventify/model/Schedule.java#L1-L105)
  - **UI Layout**: [`schedule.fxml`](src/main/resources/fxml/schedule.fxml)

---

### 1.6 Organizer Preparation Tasks & Priority Tracking (Admin-Only)
- **Preparation Workflow Management**: Create, update, delete, and filter preparation tasks assigned to teams/committees with priority levels (`Low`, `Medium`, `High`) and completion statuses (`Pending`, `In Progress`, `Completed`).
  - **Task Controller**: [`TaskController.java`](src/main/java/com/eventify/controller/TaskController.java#L31-L360)
  - **Task DAO**: [`TaskDAO.java`](src/main/java/com/eventify/dao/TaskDAO.java#L19-L141)
  - **Task & Priority Models**: [`Task.java`](src/main/java/com/eventify/model/Task.java#L1-L105) & [`TaskPriority.java`](src/main/java/com/eventify/model/TaskPriority.java#L1-L26)
  - **UI Layout**: [`tasks.fxml`](src/main/resources/fxml/tasks.fxml)

---

### 1.7 Competition Leaderboard & Automated/Custom Ranking
- **Automatic Score Sorting & Custom Rank Override**: Supports both automatic rank recalculation by descending score (`Comparable<Result>`) and explicit custom rank assignment (`1, 2, 3...`), with per-event filtering.
  - **Leaderboard Controller**: [`LeaderboardController.handleSaveResult()`](src/main/java/com/eventify/controller/LeaderboardController.java#L258-L298), [`handleUpdateResult()`](src/main/java/com/eventify/controller/LeaderboardController.java#L301-L354), [`handleDeleteResult()`](src/main/java/com/eventify/controller/LeaderboardController.java#L357-L380)
  - **Ranking & Batch Update DAO**: [`ResultDAO.recalculateAllRanks()`](src/main/java/com/eventify/dao/ResultDAO.java#L162-L184) & [`ResultDAO.findByEventId()`](src/main/java/com/eventify/dao/ResultDAO.java#L135-L160)
  - **Comparable Result Model**: [`Result.compareTo()`](src/main/java/com/eventify/model/Result.java#L99-L103)
  - **UI Layout**: [`leaderboard.fxml`](src/main/resources/fxml/leaderboard.fxml)

---

### 1.8 External REST API Integration & Jackson JSON Parsing
- **HTTP Client + Jackson `ObjectMapper` Pipeline**: Fetches external events over HTTP (`java.net.http.HttpClient`), parses JSON payloads into [`ApiEvent`](src/main/java/com/eventify/model/ApiEvent.java#L11-L115) objects using **Jackson** (`ObjectMapper` & `JsonNode`), handles network timeouts/offline states with built-in JSON fallback data, and allows importing external API events directly into the local SQLite database.
  - **REST API & Jackson Service**: [`ApiEventService.fetchEventsFromApi()`](src/main/java/com/eventify/service/ApiEventService.java#L47-L84) & [`ApiEventService.parseEventsFromJson()`](src/main/java/com/eventify/service/ApiEventService.java#L89-L133)
  - **API Event Controller**: [`ApiEventController.handleFetchFromApi()`](src/main/java/com/eventify/controller/ApiEventController.java#L117-L157) & [`ApiEventController.handleSaveSelectedToDb()`](src/main/java/com/eventify/controller/ApiEventController.java#L186-L239)
  - **UI Layout**: [`api-events.fxml`](src/main/resources/fxml/api-events.fxml)
- **JSON File Import & Export (`Jackson ObjectMapper`)**: Export single or multiple events to pretty-printed `.json` files and import `.json` event files into SQLite.
  - **JSON Utility**: [`JsonUtil.java`](src/main/java/com/eventify/util/JsonUtil.java#L19-L115)
  - **Annotated Model**: [`Event.java`](src/main/java/com/eventify/model/Event.java#L20-L157)
  - **Import/Export Handlers**: [`EventController.handleExportJson()`](src/main/java/com/eventify/controller/EventController.java#L469-L497) & [`EventController.handleImportJson()`](src/main/java/com/eventify/controller/EventController.java#L500-L535)

---

### 1.9 Responsive Pure-FXML GUI & Browser-Style Navigation History
- **Back / Forward History Stack & Breadcrumb Trail**: Tracks visited screens and supports back/forward navigation with dynamic page titles and breadcrumb trails.
  - **Scene Navigator**: [`SceneNavigator.java`](src/main/java/com/eventify/util/SceneNavigator.java#L22-L195)
- **Dynamic Table Column Binding (No CSS Files)**: Proportionally binds `TableView` column widths to window resize events purely in Java/FXML without external `.css` stylesheets.
  - **Responsive Helper**: [`ResponsiveHelper.bindColumnWidths()`](src/main/java/com/eventify/util/ResponsiveHelper.java#L18-L41)

---

## 2. APL Course Topic Coverage Matrix

| APL Topic | Eventify Implementation | Representative Code Links |
| :--- | :--- | :--- |
| **Java OOP (Inheritance, Polymorphism, Abstraction)** | Abstract `User` base class with `Organizer` and `Participant` subclasses overriding `getAccessSummary()` and `canManageSystem()`, plus Template Method `getDisplayInfo()` | [`User.java`](src/main/java/com/eventify/model/User.java#L8-L47), [`Organizer.java`](src/main/java/com/eventify/model/Organizer.java#L7-L38), [`Participant.java`](src/main/java/com/eventify/model/Participant.java#L9-L95) |
| **Interfaces, Generics & Comparable** | Generic `CrudDAO<T, ID>` implemented across 7 DAOs; `Searchable`, `Exportable`, and `Comparable<Result>` for sorting leaderboard scores | [`CrudDAO.java`](src/main/java/com/eventify/dao/CrudDAO.java#L12-L18), [`Searchable.java`](src/main/java/com/eventify/model/Searchable.java), [`Exportable.java`](src/main/java/com/eventify/model/Exportable.java), [`Result.java`](src/main/java/com/eventify/model/Result.java#L99-L103) |
| **Custom Exceptions** | Unchecked `DatabaseException` wrapping `SQLException` and concurrency errors across DAO and Service layers | [`DatabaseException.java`](src/main/java/com/eventify/exception/DatabaseException.java#L1-L18) |
| **JavaFX Desktop GUI & FXML** | 8 responsive FXML views styled strictly via inline FXML/Java properties (zero `.css` files) | [`src/main/resources/fxml/`](src/main/resources/fxml/) |
| **SQLite + JDBC Relational Database** | 8 relational tables (`users`, `events`, `participants`, `registrations`, `attendance`, `schedules`, `tasks`, `results`) with PK, FK (`ON DELETE CASCADE`), `CHECK` constraints, and `PreparedStatement` queries | [`DatabaseConnection.java`](src/main/java/com/eventify/database/DatabaseConnection.java#L16-L42), [`DatabaseInitializer.java`](src/main/java/com/eventify/database/DatabaseInitializer.java#L19-L173) |
| **Multithreading & Concurrency** | Fixed daemon thread pool (`AppExecutor`), parallel `Future<T>` queries in `DashboardService`, and non-blocking JavaFX `Task<T>` in all controllers | [`AppExecutor.java`](src/main/java/com/eventify/util/AppExecutor.java#L15-L52), [`DashboardService.java`](src/main/java/com/eventify/service/DashboardService.java#L36-L61) |
| **JSON Serialization & Parsing (Jackson)** | Jackson `ObjectMapper`, `JsonNode`, and `@JsonIgnoreProperties` for importing/exporting events and parsing REST API responses | [`JsonUtil.java`](src/main/java/com/eventify/util/JsonUtil.java#L19-L115), [`ApiEventService.java`](src/main/java/com/eventify/service/ApiEventService.java#L89-L133) |
| **HTTP REST API Handling** | `java.net.http.HttpClient`, `HttpRequest`, and `HttpResponse` with timeout handling, status code validation, and offline fallback | [`ApiEventService.java`](src/main/java/com/eventify/service/ApiEventService.java#L31-L84) |

---

## 3. Project Structure

```text
Eventify/
├── pom.xml                                      # Maven configuration (JavaFX 21, SQLite JDBC, Jackson Databind)
├── data/eventify.db                             # Auto-initialized local SQLite relational database
└── src/main/
    ├── java/com/eventify/
    │   ├── Main.java                            # Launcher entry point
    │   ├── EventifyApp.java                     # JavaFX Application startup & DB initialization
    │   ├── controller/                          # 8 JavaFX FXML Controllers
    │   ├── dao/                                 # Generic CrudDAO<T, ID> & 7 JDBC DAO implementations
    │   ├── database/                            # DatabaseConnection & DatabaseInitializer (8 tables + seed data)
    │   ├── exception/                           # Custom DatabaseException
    │   ├── model/                               # Domain models, Enums, Interfaces & OOP hierarchy
    │   ├── service/                             # Business logic, concurrent DashboardService & ApiEventService
    │   └── util/                                # AppExecutor, JsonUtil (Jackson), SceneNavigator, SessionManager
    └── resources/fxml/                          # 8 responsive FXML views (no external CSS)
```

---

## 4. Default Login Credentials

| Role | Roll / User ID | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **Organizer (Admin — EVA PODDER)** | `admin` | `admin123` | Full administrative access to all modules, CRUD operations, Participants & Attendance, and Tasks |
| **Participant (Student)** | `2307032` | `student123` | Browse open/closed events, 1-click event registration by Roll, view personal registrations, Schedule & Leaderboard |

> **Tip:** You can also register any new student Roll (e.g., `2307035`) directly from the login screen by entering your Roll and password and clicking **Sign Up**.

---

## 5. How to Build & Run

### Option 1: Using Maven CLI / Wrapper
```bash
./mvnw clean javafx:run
```
or with a local Maven installation:
```bash
mvn clean javafx:run
```

### Option 2: In IntelliJ IDEA
1. Open the project folder (`Eventify`) in **IntelliJ IDEA**.
2. Open the **Maven** tool window → **Eventify** → **Plugins** → **javafx** → double-click **`javafx:run`** (or run [`Main.java`](src/main/java/com/eventify/Main.java)).
