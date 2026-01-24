# Hotel Booking System

A Java desktop application for managing hotel bookings, built with JavaFX. This teaching project demonstrates Object-Oriented Programming, file handling, and GUI development fundamentals.

## Features

- **Room Management** - Add, edit, and delete rooms with different types (Single, Double, Suite)
- **Guest Management** - Maintain guest records with contact information
- **Booking System** - Create bookings with automatic price calculation including VAT
- **Invoice Generation** - Generate detailed invoices for bookings
- **Cancellation & Refunds** - Cancel bookings with tiered refund policies
- **Dashboard** - View statistics on rooms, availability, and active bookings

## Prerequisites (Windows)

### Install Java 17

**Option 1: Download Installer (Recommended)**
1. Download OpenJDK 17 from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
2. Choose Windows x64 `.msi` installer
3. Run the installer and check "Set JAVA_HOME variable" during installation

**Option 2: Using Chocolatey**
```cmd
choco install temurin17
```

### Install Maven

**Option 1: Download and Configure**
1. Download Maven from [maven.apache.org](https://maven.apache.org/download.cgi) (Binary zip archive)
2. Extract to `C:\Program Files\Apache\maven`
3. Add to System Environment Variables:
   - Add `C:\Program Files\Apache\maven\bin` to `PATH`
   - Create `MAVEN_HOME` variable set to `C:\Program Files\Apache\maven`

**Option 2: Using Chocolatey**
```cmd
choco install maven
```

### Verify Installation

Open Command Prompt or PowerShell and run:

```cmd
java -version
```
Should display: `openjdk version "17.x.x"`

```cmd
mvn -version
```
Should display: `Apache Maven 3.x.x`

## Installation

1. **Clone or download the project**
   ```cmd
   git clone <repository-url>
   cd BookingSystem
   ```

2. **Compile the project**
   ```cmd
   mvn compile
   ```

3. **Run tests (optional)**
   ```cmd
   mvn test
   ```

## Running the Application

Start the application with:

```cmd
mvn javafx:run
```

The Hotel Booking System window will open, displaying the Dashboard.

## Project Structure

```
BookingSystem/
├── pom.xml                          # Maven build configuration
├── data/                            # JSON data files (auto-created)
│   ├── settings.json                # VAT rate, currency, hotel name
│   ├── rooms.json                   # Room inventory
│   ├── guests.json                  # Guest records
│   └── bookings.json                # Booking records
├── src/main/java/com/example/hotel/
│   ├── App.java                     # Main application entry point
│   ├── model/                       # Data classes (Room, Guest, Booking, Invoice)
│   ├── service/                     # Business logic layer
│   ├── persistence/                 # Data access (JSON file handling)
│   ├── gui/controllers/             # JavaFX screen controllers
│   └── util/                        # Helper utilities
└── src/main/resources/
    ├── styles/application.css       # Application styling
    └── com/example/hotel/gui/fxml/  # Screen layouts
```

## Usage Guide

### Dashboard

The main screen shows:
- Total number of rooms in the system
- Currently available rooms
- Active bookings count
- Navigation buttons to other screens

### Managing Rooms

1. Click **"Rooms"** from the Dashboard
2. View all rooms in the table
3. Use **"Add Room"** to create a new room:
   - Enter room number
   - Select room type (Single, Double, Suite)
   - Set price per night
   - Choose if room is refundable
4. Select a room and click **"Edit"** or **"Delete"** to modify

### Managing Guests

1. Click **"Guests"** from the Dashboard
2. View all registered guests
3. Use **"Add Guest"** to register a new guest:
   - Enter name, phone, and email
4. Select a guest to edit their information

### Creating a Booking

1. Click **"New Booking"** from the Dashboard
2. Select a guest from the dropdown
3. Select an available room
4. Choose check-in and check-out dates
5. Review the calculated price (includes 10% VAT)
6. Click **"Confirm Booking"**

### Viewing Bookings

1. Click **"Bookings"** from the Dashboard
2. View all bookings with their status
3. Select a booking to:
   - **View Invoice** - See detailed cost breakdown
   - **Cancel** - Cancel the booking (refund calculated based on policy)

### Refund Policy

For refundable rooms:
- Cancel **7+ days** before check-in: **100% refund**
- Cancel **3-6 days** before check-in: **50% refund**
- Cancel **less than 3 days** before check-in: **No refund**

Non-refundable rooms receive no refund regardless of cancellation timing.

## Troubleshooting

### "java is not recognized as an internal or external command"

Java is not in your PATH. Solutions:
1. Reinstall Java using the Adoptium installer with "Set JAVA_HOME" checked
2. Or manually add Java to PATH:
   - Open System Properties > Environment Variables
   - Edit `PATH` and add `C:\Program Files\Eclipse Adoptium\jdk-17.x.x\bin`
   - Create `JAVA_HOME` pointing to `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`

### "mvn is not recognized as an internal or external command"

Maven is not in your PATH. Add the Maven `bin` directory to your PATH environment variable.

### JavaFX Graphics Device Errors

If you see graphics-related errors:
1. Update your graphics drivers
2. Try running with software rendering:
   ```cmd
   set JAVA_TOOL_OPTIONS=-Dprism.order=sw
   mvn javafx:run
   ```

### Application Won't Start / Blank Window

1. Ensure Java 17 is being used (not an older version):
   ```cmd
   java -version
   ```
2. Clean and rebuild:
   ```cmd
   mvn clean compile
   mvn javafx:run
   ```

### Data Not Saving

- Ensure the application has write permissions in the project directory
- Check that the `data/` folder exists and is not read-only

### Maven Download Errors

If Maven fails to download dependencies:
1. Check your internet connection
2. Try clearing the Maven cache:
   ```cmd
   rmdir /s /q %USERPROFILE%\.m2\repository
   mvn compile
   ```

## Build Commands Reference

| Command | Description |
|---------|-------------|
| `mvn compile` | Compile the project |
| `mvn test` | Run all unit tests |
| `mvn javafx:run` | Run the application |
| `mvn clean` | Clean build artifacts |
| `mvn clean compile` | Clean and recompile |

## OOP Concepts Demonstrated

This project demonstrates the following Object-Oriented Programming concepts:

### Encapsulation

All model classes use private fields with public getters and setters. Setters include validation logic:

```java
// Room.java - validates price cannot be negative
public void setPricePerNight(double pricePerNight) {
    if (pricePerNight < 0) {
        throw new IllegalArgumentException("Price per night cannot be negative");
    }
    this.pricePerNight = pricePerNight;
}
```

**See:** `model/Room.java`, `model/Guest.java`, `model/Booking.java`

### Polymorphism

The refund system uses interface-based polymorphism. Three different refund policies implement the same `RefundPolicy` interface:

```java
public interface RefundPolicy {
    double calculateRefund(Booking booking, LocalDate cancelDate);
}
```

| Implementation | Behavior |
|---------------|----------|
| `NoRefundPolicy` | Always returns 0 |
| `FullRefundPolicy` | Returns full booking amount |
| `TieredRefundPolicy` | 7+ days: 100%, 3-6 days: 50%, <3 days: 0% |

The `BookingService` selects the appropriate policy at runtime based on room settings.

**See:** `model/policy/RefundPolicy.java` and implementations

### Abstraction

The `Repository<T>` interface abstracts data storage operations, hiding whether data is stored in files, a database, or memory:

```java
public interface Repository<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    boolean delete(ID id);
}
```

**See:** `persistence/Repository.java`, `persistence/FileRepository.java`

### Clean Class Relationships

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

- Classes reference each other by ID (String), not direct object references
- This enables JSON serialization and loose coupling

### File I/O Persistence

Data is persisted to JSON files using the Gson library:

- **Load on startup:** `FileRepository` reads JSON into an in-memory cache
- **Save on change:** Every create/update/delete writes back to the file
- **Type adapters:** Custom adapters handle `LocalDate` and `LocalDateTime` serialization

**Data files:** `data/rooms.json`, `data/guests.json`, `data/bookings.json`, `data/settings.json`

**See:** `persistence/FileRepository.java`, `persistence/JsonUtils.java`

### JavaFX GUI with FXML

The GUI follows the Model-View-Controller pattern:

- **FXML files** define the view layout (buttons, tables, forms)
- **Controller classes** handle user interactions
- **CSS styling** provides consistent appearance

```
Dashboard.fxml  ←→  DashboardController.java
Booking.fxml    ←→  BookingController.java
Invoice.fxml    ←→  InvoiceController.java
```

**See:** `gui/fxml/` for views, `gui/controllers/` for controllers

### Separation of Concerns

The application follows a layered architecture:

```
┌──────────────────────────────────────────────────────┐
│                    GUI Layer                         │
│  Controllers handle user input, delegate to services │
│  (No business logic in controllers)                  │
├──────────────────────────────────────────────────────┤
│                  Service Layer                       │
│  Business logic: pricing, validation, refunds        │
│  (No GUI code in services)                           │
├──────────────────────────────────────────────────────┤
│               Persistence Layer                      │
│  Repository pattern abstracts file storage           │
├──────────────────────────────────────────────────────┤
│                  Data Layer                          │
│  JSON files in data/ directory                       │
└──────────────────────────────────────────────────────┘
```

**Services:** `RoomService`, `GuestService`, `BookingService`, `InvoiceService`

## License

This is an educational project for learning Java fundamentals.
