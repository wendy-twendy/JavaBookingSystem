# Hotel Booking System - Student Q&A Guide

A comprehensive guide answering questions a professor might ask about this project. Use this to understand how the codebase implements key Java and OOP concepts.

---

## Table of Contents

1. [Object-Oriented Programming](#1-object-oriented-programming)
2. [Model Layer](#2-model-layer)
3. [Persistence & File I/O](#3-persistence--file-io)
4. [Service Layer & Business Logic](#4-service-layer--business-logic)
5. [JavaFX GUI](#5-javafx-gui)
6. [Architecture & Design Patterns](#6-architecture--design-patterns)

---

## 1. Object-Oriented Programming

### Q: What is encapsulation and where is it used in this project?

**Answer:** Encapsulation is hiding internal data and providing controlled access through methods. In this project, all model classes use private fields with public getters and setters.

**Example from `Room.java`:**
```java
public class Room {
    private String roomNumber;      // Private - cannot access directly
    private RoomType type;
    private double pricePerNight;
    private boolean available;
    private boolean refundable;

    // Controlled access through getter
    public double getPricePerNight() {
        return pricePerNight;
    }

    // Setter with validation - prevents invalid state
    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.pricePerNight = pricePerNight;
    }
}
```

**Why it matters:** Without encapsulation, any code could set `pricePerNight = -100`, creating invalid data. The setter validates input before accepting it.

**See:** `src/main/java/com/example/hotel/model/Room.java`

---

### Q: What is polymorphism and how is it demonstrated?

**Answer:** Polymorphism means "many forms" - the same interface can have different implementations. This project demonstrates it through the `RefundPolicy` interface.

**The Interface (`RefundPolicy.java`):**
```java
public interface RefundPolicy {
    double calculateRefund(Booking booking, LocalDate cancelDate);
    String getDescription();
}
```

**Three Different Implementations:**

| Class | Behavior |
|-------|----------|
| `NoRefundPolicy` | Always returns 0 |
| `FullRefundPolicy` | Returns 100% of booking cost |
| `TieredRefundPolicy` | Returns 100%, 50%, or 0% based on timing |

**How it's used in `BookingService.java`:**
```java
// The service doesn't know which policy it's using - just calls the interface
RefundPolicy policy = getRefundPolicy(room);  // Could be any of the 3 types
double refundAmount = policy.calculateRefund(booking, LocalDate.now());
```

**Why it matters:** You can add new refund policies (like `HolidayRefundPolicy`) without changing the `BookingService` code.

**See:** `src/main/java/com/example/hotel/model/policy/`

---

### Q: Explain how TieredRefundPolicy calculates the refund step by step.

**Answer:** The tiered policy gives different refund percentages based on how many days before check-in you cancel.

**The Rules:**
- Cancel 7+ days before check-in → 100% refund
- Cancel 3-6 days before check-in → 50% refund
- Cancel less than 3 days before → No refund

**Code walkthrough (`TieredRefundPolicy.java`):**
```java
public double calculateRefund(Booking booking, LocalDate cancelDate) {
    // Step 1: Get the check-in date from the booking
    LocalDate checkInDate = booking.getCheckInDate();

    // Step 2: Calculate days between cancel date and check-in
    long daysUntilCheckIn = ChronoUnit.DAYS.between(cancelDate, checkInDate);

    // Step 3: Apply the appropriate refund rate
    if (daysUntilCheckIn >= 7) {
        return booking.getTotalCost();              // 100% refund
    } else if (daysUntilCheckIn >= 3) {
        return booking.getTotalCost() * 0.50;       // 50% refund
    } else {
        return 0.0;                                  // No refund
    }
}
```

**Example Calculation:**
```
Booking: Check-in January 27, Total Cost $500
Cancel Date: January 20

Step 1: checkInDate = January 27
Step 2: daysUntilCheckIn = Days.between(Jan 20, Jan 27) = 7
Step 3: 7 >= 7? YES → Return $500 (100% refund)
```

---

### Q: What is abstraction and where is it used?

**Answer:** Abstraction hides complex implementation details behind a simple interface. The `Repository<T>` interface abstracts data storage.

**The Interface (`Repository.java`):**
```java
public interface Repository<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    boolean delete(ID id);
}
```

**Why it's abstract:** The interface doesn't say HOW data is stored. It could be:
- JSON files (current implementation)
- A database
- Cloud storage
- In-memory only

The service layer just calls `repository.save(booking)` without knowing it's writing to a JSON file.

**See:** `src/main/java/com/example/hotel/persistence/Repository.java`

---

## 2. Model Layer

### Q: How do the model classes relate to each other?

**Answer:** The classes reference each other using String IDs (foreign keys), not direct object references.

**Relationship Diagram:**
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Booking   │────▶│    Guest    │     │    Room     │
│  (guestId)  │     │    (id)     │     │(roomNumber) │
│ (roomNumber)│────▶│             │     │             │
└─────────────┘     └─────────────┘     └─────────────┘
       │
       ▼
┌─────────────┐
│   Invoice   │
│ (bookingId) │
└─────────────┘
```

**Booking.java fields:**
```java
private String bookingId;      // Primary key
private String guestId;        // References Guest.id
private String roomNumber;     // References Room.roomNumber
private LocalDate checkInDate;
private LocalDate checkOutDate;
private BookingStatus status;
private double totalCost;
```

**Why String IDs instead of object references?**
1. Easier JSON serialization
2. Loose coupling between classes
3. Service layer can join data from different repositories

---

### Q: How does getNumberOfNights() work in Booking?

**Answer:** It calculates the number of days between check-in and check-out dates.

```java
public long getNumberOfNights() {
    if (checkInDate == null || checkOutDate == null) {
        return 0;  // Defensive programming - prevent NullPointerException
    }
    return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
}
```

**Examples:**
| Check-in | Check-out | Nights |
|----------|-----------|--------|
| Jan 24 | Jan 27 | 3 |
| Jan 24 | Jan 25 | 1 |
| Jan 24 | Jan 24 | 0 (invalid) |

**Note:** The `ChronoUnit.DAYS.between()` method is part of Java's `java.time` API - the modern way to handle dates.

---

### Q: Why do the enums have extra fields like displayName?

**Answer:** Enums can hold data and behavior, not just constant values. This makes them more useful than simple strings.

**RoomType enum:**
```java
public enum RoomType {
    SINGLE("Single Room", 1),
    DOUBLE("Double Room", 2),
    SUITE("Suite", 4);

    private final String displayName;
    private final int maxOccupancy;

    RoomType(String displayName, int maxOccupancy) {
        this.displayName = displayName;
        this.maxOccupancy = maxOccupancy;
    }

    public String getDisplayName() {
        return displayName;  // "Single Room" instead of "SINGLE"
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }
}
```

**Benefits:**
- GUI shows "Single Room" not "SINGLE"
- Can validate: `if (guests > roomType.getMaxOccupancy()) throw error;`
- Type-safe: compiler prevents typos like `"SINGEL"`

---

## 3. Persistence & File I/O

### Q: How does data get saved to JSON files?

**Answer:** The `FileRepository` class handles all file operations using the Gson library.

**Save Operation (`FileRepository.java`):**
```java
@Override
public T save(T entity) {
    ID id = idExtractor.apply(entity);  // Get entity's ID

    // Remove old version if exists (for updates)
    cache.removeIf(e -> idExtractor.apply(e).equals(id));

    // Add new/updated entity to cache
    cache.add(entity);

    // Write entire cache to file
    persist();

    return entity;
}

private void persist() {
    String json = JsonUtils.toJson(cache);      // Convert to JSON
    Files.writeString(filePath, json);          // Write to file
}
```

**Flow: Service → Repository → File**
```
bookingService.createBooking(...)
    ↓
bookingRepository.save(booking)
    ↓
JsonUtils.toJson(cache)
    ↓
Files.writeString("data/bookings.json", json)
```

---

### Q: What are type adapters and why are they needed?

**Answer:** Type adapters tell Gson how to convert Java objects to/from JSON. They're needed for `LocalDate` because Gson doesn't know how to handle it by default.

**Without adapter, LocalDate would serialize as:**
```json
{
  "checkInDate": {
    "year": 2026,
    "month": "JANUARY",
    "dayOfMonth": 24
  }
}
```

**With adapter, it serializes cleanly:**
```json
{
  "checkInDate": "2026-01-24"
}
```

**The adapter code (`JsonUtils.java`):**
```java
private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        out.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        return LocalDate.parse(in.nextString(), DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
```

---

### Q: What is the Singleton pattern and how is it used in Settings?

**Answer:** Singleton ensures only ONE instance of a class exists. `Settings` uses this because the entire application should share one configuration.

**Implementation (`Settings.java`):**
```java
public final class Settings {
    private static volatile Settings instance;  // The single instance

    // Private constructor - prevents new Settings()
    private Settings() {
        load();
    }

    // Public method to get the instance
    public static synchronized Settings getInstance() {
        if (instance == null) {
            instance = new Settings();  // Created only once
        }
        return instance;
    }
}
```

**Usage throughout the application:**
```java
// In BookingService
Settings settings = Settings.getInstance();
double vatRate = settings.getVatRate();

// In InvoiceService (same instance!)
Settings settings = Settings.getInstance();
String currency = settings.getCurrency();
```

**Why Singleton here?**
- All services need the same VAT rate
- Changing VAT in one place affects entire application
- Only loads settings.json once

---

## 4. Service Layer & Business Logic

### Q: Walk through what happens when a booking is created.

**Answer:** The `createBooking()` method in `BookingService` performs multiple validations before saving.

**Step-by-step (`BookingService.java`):**

```java
public Booking createBooking(String guestId, String roomNumber,
                              LocalDate checkIn, LocalDate checkOut) {

    // Step 1: Validate dates
    validateBookingDates(checkIn, checkOut);
    // Throws exception if checkIn >= checkOut or dates in past

    // Step 2: Check room exists
    Room room = roomService.findByRoomNumber(roomNumber)
            .orElseThrow(() -> new IllegalArgumentException(
                "Room " + roomNumber + " not found"));

    // Step 3: Check room is available
    if (!room.isAvailable()) {
        throw new IllegalArgumentException(
            "Room " + roomNumber + " is not available");
    }

    // Step 4: Check for overlapping bookings
    if (hasOverlappingBooking(roomNumber, checkIn, checkOut)) {
        throw new IllegalArgumentException(
            "Room has conflicting bookings for these dates");
    }

    // Step 5: Calculate total cost (nights × price × (1 + VAT))
    double totalCost = calculateTotalCost(room, checkIn, checkOut);

    // Step 6: Create and save booking
    Booking booking = new Booking(
        generateBookingId(),    // "BK-" + UUID
        guestId,
        roomNumber,
        checkIn,
        checkOut,
        BookingStatus.CONFIRMED,
        totalCost
    );
    bookingRepository.save(booking);

    // Step 7: Mark room as unavailable
    roomService.setAvailability(roomNumber, false);

    return booking;
}
```

---

### Q: How does the system detect overlapping bookings?

**Answer:** Two date ranges overlap if each one starts before the other ends.

**The algorithm:**
```java
private boolean datesOverlap(LocalDate start1, LocalDate end1,
                             LocalDate start2, LocalDate end2) {
    return start1.isBefore(end2) && start2.isBefore(end1);
}
```

**Visual example:**
```
Existing Booking: Jan 10 ═══════ Jan 15
New Request:           Jan 12 ═══════════ Jan 20

Check: Jan 10 < Jan 20? YES
       Jan 12 < Jan 15? YES

Result: OVERLAP! Booking rejected.
```

**Non-overlapping example:**
```
Existing Booking: Jan 10 ═══════ Jan 15
New Request:                          Jan 16 ═══════ Jan 20

Check: Jan 10 < Jan 20? YES
       Jan 16 < Jan 15? NO

Result: NO OVERLAP. Booking allowed.
```

---

### Q: How is the total price calculated?

**Answer:** The formula is: `nights × pricePerNight × (1 + vatRate)`

**Code (`BookingService.java`):**
```java
public double calculateTotalCost(Room room, LocalDate checkIn, LocalDate checkOut) {
    long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
    double subtotal = nights * room.getPricePerNight();
    double vat = subtotal * settings.getVatRate();
    return subtotal + vat;
}
```

**Example:**
```
Room price: $100/night
VAT rate: 20% (0.20)
Check-in: Jan 24
Check-out: Jan 27

Calculation:
  nights = 3
  subtotal = 3 × $100 = $300
  vat = $300 × 0.20 = $60
  total = $300 + $60 = $360
```

---

### Q: What happens when a booking is cancelled?

**Answer:** The system calculates the refund, updates the booking status, and makes the room available again.

```java
public Booking cancelBooking(String bookingId) {
    // Step 1: Find the booking
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

    // Step 2: Validate it can be cancelled
    if (booking.getStatus() == BookingStatus.CANCELLED) {
        throw new IllegalArgumentException("Already cancelled");
    }
    if (booking.getStatus() == BookingStatus.COMPLETED) {
        throw new IllegalArgumentException("Cannot cancel completed booking");
    }

    // Step 3: Get refund policy based on room
    Room room = roomService.findByRoomNumber(booking.getRoomNumber()).orElse(null);
    RefundPolicy policy = getRefundPolicy(room);  // Polymorphism!

    // Step 4: Calculate refund
    double refundAmount = policy.calculateRefund(booking, LocalDate.now());

    // Step 5: Update booking
    booking.setStatus(BookingStatus.CANCELLED);
    booking.setRefundAmount(refundAmount);
    bookingRepository.save(booking);

    // Step 6: Free the room
    if (room != null) {
        roomService.setAvailability(room.getRoomNumber(), true);
    }

    return booking;
}
```

---

## 5. JavaFX GUI

### Q: How do FXML files connect to Controller classes?

**Answer:** The `fx:controller` attribute in FXML specifies which Java class handles the UI logic.

**In Dashboard.fxml:**
```xml
<BorderPane xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.example.hotel.gui.controllers.DashboardController">
```

**The connection:**
1. `FXMLLoader` reads the FXML file
2. Sees `fx:controller` and creates a `new DashboardController()`
3. Finds all `fx:id` attributes and injects them into `@FXML` fields
4. Calls the `initialize()` method

---

### Q: What does @FXML do?

**Answer:** `@FXML` marks fields and methods that should be connected to FXML elements.

**In Dashboard.fxml:**
```xml
<Label fx:id="totalRoomsLabel" text="0"/>
<Button text="Manage Rooms" onAction="#handleManageRooms"/>
```

**In DashboardController.java:**
```java
@FXML
private Label totalRoomsLabel;  // Injected from fx:id="totalRoomsLabel"

@FXML
private void handleManageRooms() {  // Called by onAction="#handleManageRooms"
    App.showRoomManagement();
}
```

**The flow:**
1. FXML: `fx:id="totalRoomsLabel"` → Controller: `@FXML Label totalRoomsLabel`
2. FXML: `onAction="#handleManageRooms"` → Controller: `@FXML void handleManageRooms()`

---

### Q: When is initialize() called and what should go there?

**Answer:** `initialize()` is called automatically AFTER all `@FXML` fields are injected. Use it for setup that needs UI elements.

```java
@FXML
public void initialize() {
    // At this point, all @FXML fields are ready to use

    // Load initial data
    hotelNameLabel.setText(settings.getHotelName());

    // Populate dropdowns
    setupGuestCombo();
    setupRoomCombo();

    // Load statistics
    refreshStatistics();
}
```

**Timeline:**
```
1. FXMLLoader creates Controller instance (constructor runs)
2. FXMLLoader injects @FXML fields
3. FXMLLoader calls initialize()
4. Scene is displayed
```

**Warning:** Don't access `@FXML` fields in the constructor - they're null until after injection!

---

### Q: How are TableViews populated with data?

**Answer:** TableViews use `setCellValueFactory` to extract data from model objects for each column.

**Setting up columns (`BookingListController.java`):**
```java
private void setupTableColumns() {
    // Column 1: Direct property access
    colBookingId.setCellValueFactory(data ->
        new SimpleStringProperty(data.getValue().getBookingId()));

    // Column 2: Property with transformation
    colTotal.setCellValueFactory(data ->
        new SimpleStringProperty(String.format("$%.2f",
            data.getValue().getTotalCost())));

    // Column 3: Property with lookup (join)
    colGuestName.setCellValueFactory(data -> {
        String guestId = data.getValue().getGuestId();
        return guestService.findById(guestId)
            .map(Guest::getName)
            .map(SimpleStringProperty::new)
            .orElse(new SimpleStringProperty("Unknown"));
    });

    // Bind the data list to the table
    bookingTable.setItems(bookingList);
}
```

**Loading data:**
```java
private void loadBookings() {
    List<Booking> bookings = bookingService.getAllBookings();
    bookingList.setAll(bookings);  // Triggers table refresh
}
```

---

### Q: How does navigation between screens work?

**Answer:** The `App` class has static methods that load FXML files and swap the scene.

**In App.java:**
```java
public static void showDashboard() {
    Parent root = loadView("Dashboard");  // Loads Dashboard.fxml
    setScene(root);
}

public static void showBooking() {
    Parent root = loadView("Booking");    // Loads Booking.fxml
    setScene(root);
}

private static void setScene(Parent root) {
    primaryStage.setScene(new Scene(root));
}
```

**In any Controller (e.g., DashboardController):**
```java
@FXML
private void handleNewBooking() {
    App.showBooking();  // Navigate to booking screen
}
```

---

## 6. Architecture & Design Patterns

### Q: Explain the layered architecture of this project.

**Answer:** The project separates concerns into distinct layers, each with a specific responsibility.

```
┌─────────────────────────────────────────────────────────────────┐
│                        GUI LAYER                                 │
│  Controllers handle user input and update the display           │
│  - DashboardController, BookingController, etc.                 │
│  - NO business logic here                                       │
├─────────────────────────────────────────────────────────────────┤
│                      SERVICE LAYER                               │
│  Business logic: pricing, validation, refunds                   │
│  - BookingService, RoomService, GuestService, InvoiceService    │
│  - NO GUI code here                                             │
├─────────────────────────────────────────────────────────────────┤
│                    PERSISTENCE LAYER                             │
│  Data access: reading and writing to storage                    │
│  - Repository interface, FileRepository, JsonUtils              │
│  - NO business logic here                                       │
├─────────────────────────────────────────────────────────────────┤
│                       DATA LAYER                                 │
│  Physical storage                                                │
│  - data/rooms.json, data/bookings.json, etc.                   │
└─────────────────────────────────────────────────────────────────┘
```

**Data flow example - Creating a booking:**
```
User clicks "Confirm Booking"
       ↓
BookingController.handleConfirm()     [GUI]
       ↓
bookingService.createBooking(...)     [Service - validates, calculates]
       ↓
bookingRepository.save(booking)       [Persistence]
       ↓
Files.writeString("data/bookings.json", json)  [Data]
```

---

### Q: What design patterns are used in this project?

**Answer:**

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Singleton** | `Settings`, `RepositoryFactory` | Single shared instance |
| **Repository** | `Repository<T>`, `FileRepository` | Abstract data access |
| **Strategy** | `RefundPolicy` + implementations | Interchangeable algorithms |
| **Factory** | `RepositoryFactory` | Centralized object creation |
| **MVC** | FXML (View) + Controller + Model | Separate UI from logic |

---

### Q: Why don't Controllers contain business logic?

**Answer:** Separation of concerns makes code easier to test, maintain, and reuse.

**Bad (logic in controller):**
```java
// DON'T DO THIS
@FXML
private void handleConfirm() {
    // Business logic mixed with UI
    long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
    double subtotal = nights * room.getPricePerNight();
    double vat = subtotal * 0.10;
    double total = subtotal + vat;
    // Save to file directly...
}
```

**Good (delegate to service):**
```java
// DO THIS
@FXML
private void handleConfirm() {
    try {
        // Controller just coordinates
        Booking booking = bookingService.createBooking(
            guestId, roomNumber, checkIn, checkOut);
        AlertUtil.showSuccess("Booking created!");
        App.showBookingList();
    } catch (IllegalArgumentException e) {
        AlertUtil.showError("Error", e.getMessage());
    }
}
```

**Benefits:**
- Service can be tested without GUI
- Multiple UIs can share the same service
- Business rules are in one place

---

### Q: What is Optional and why is it used instead of null?

**Answer:** `Optional<T>` is a container that may or may not contain a value. It forces you to handle the "not found" case.

**Without Optional (dangerous):**
```java
Room room = roomService.findByRoomNumber("999");  // Returns null
double price = room.getPricePerNight();  // NullPointerException!
```

**With Optional (safe):**
```java
Optional<Room> roomOpt = roomService.findByRoomNumber("999");

// Option 1: Check if present
if (roomOpt.isPresent()) {
    Room room = roomOpt.get();
    double price = room.getPricePerNight();
}

// Option 2: Provide default
Room room = roomOpt.orElse(new Room("DEFAULT", RoomType.SINGLE, 0, true, true));

// Option 3: Throw if missing
Room room = roomOpt.orElseThrow(() ->
    new IllegalArgumentException("Room not found"));
```

---

## Quick Reference

### File Locations

| Component | Location |
|-----------|----------|
| Models | `src/main/java/com/example/hotel/model/` |
| Services | `src/main/java/com/example/hotel/service/` |
| Persistence | `src/main/java/com/example/hotel/persistence/` |
| Controllers | `src/main/java/com/example/hotel/gui/controllers/` |
| FXML Views | `src/main/resources/com/example/hotel/gui/fxml/` |
| CSS Styles | `src/main/resources/styles/application.css` |
| Data Files | `data/*.json` |

### Key Classes to Study

| Concept | Class | Key Methods |
|---------|-------|-------------|
| Encapsulation | `Room.java` | `setPricePerNight()` |
| Polymorphism | `TieredRefundPolicy.java` | `calculateRefund()` |
| Repository Pattern | `FileRepository.java` | `save()`, `findById()` |
| Singleton | `Settings.java` | `getInstance()` |
| Business Logic | `BookingService.java` | `createBooking()`, `cancelBooking()` |
| FXML Binding | `DashboardController.java` | `initialize()`, `@FXML` fields |
| TableView | `BookingListController.java` | `setupTableColumns()` |

---

## Practice Exercises

1. **Trace a booking creation** - Follow the code from button click to JSON file write
2. **Add a new refund policy** - Create `WeekendRefundPolicy` that gives 75% refund on weekends
3. **Add a new field** - Add `numberOfGuests` to `Booking` and update all layers
4. **Test the validation** - Try creating a booking with invalid dates and see what happens
5. **Read the JSON files** - Open `data/bookings.json` and match fields to `Booking.java`

---

*This guide is meant to complement hands-on exploration of the code. Open the files mentioned and trace through the logic yourself!*
