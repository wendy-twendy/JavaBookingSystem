# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Hotel Booking System** - a teaching project for Java fundamentals using OOP, file handling, and JavaFX GUI programming.

## Prerequisites & Installation

**Java 17 and Maven are required.** Install via Homebrew on macOS:

```bash
# Install Java 17
brew install openjdk@17

# Set environment variables (add to ~/.zshrc or ~/.bash_profile)
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Install Maven
brew install maven

# Verify installation
java -version   # Should show openjdk 17.x
mvn -version    # Should show Apache Maven 3.x
```

## Build Commands

```bash
mvn compile              # Compile the project
mvn test                 # Run all tests
mvn test -Dtest=RoomServiceTest  # Run a single test class
mvn javafx:run           # Run the JavaFX application
mvn clean                # Clean build artifacts
```

## Project Structure

```
BookingSystem/
├── pom.xml                          # Maven build (Java 17, JavaFX 21.0.2, Gson 2.10.1, JUnit 5.9.2)
├── data/                            # JSON data files
│   ├── settings.json                # VAT rate, currency, hotel name
│   ├── rooms.json                   # Room inventory
│   ├── guests.json                  # Guest records
│   └── bookings.json                # Booking records
├── src/main/java/com/example/hotel/
│   ├── App.java                     # Main JavaFX application
│   ├── model/
│   │   ├── enums/                   # RoomType, BookingStatus
│   │   ├── policy/                  # RefundPolicy interface + implementations
│   │   ├── Room.java, Guest.java, Booking.java, Invoice.java
│   ├── service/                     # Business logic layer
│   ├── persistence/                 # Repository pattern for data access
│   ├── gui/controllers/             # JavaFX controllers (DashboardController, etc.)
│   └── util/                        # Helper utilities
└── src/main/resources/
    ├── styles/application.css       # Application-wide CSS styles
    └── com/example/hotel/gui/fxml/  # FXML view files (Dashboard.fxml, etc.)
```

## Architecture

**Layered architecture with strict separation of concerns:**

```
GUI (Controllers) → Services → Repositories → File System (JSON)
         ↓              ↓            ↓
       FXML          Models     data/*.json
```

- **Controllers**: Thin, delegate to services, no business logic
- **Services**: Business logic only, no GUI code
- **Repositories**: Generic `Repository<T>` interface with `FileRepository<T>` implementation
- **Models**: POJOs extending `AbstractEntity` base class, with validation in setters

## Key Implementation Notes

### Abstract Classes

Three abstract classes reduce code duplication and demonstrate OOP inheritance:

**`AbstractEntity`** (model base class):
- All 4 model classes (Room, Guest, Booking, Invoice) extend this
- Provides shared `equals()`/`hashCode()` based on `getId()`
- Each subclass implements `abstract String getId()` returning its specific ID field

**`AbstractRefundPolicy`** (policy base class):
- Sits between `RefundPolicy` interface and concrete implementations
- Provides shared `policyName` field, `getPolicyName()`, and `toString()`
- Hierarchy: `RefundPolicy` (interface) → `AbstractRefundPolicy` (abstract) → concrete classes

**`AbstractService<T>`** (service base class):
- All 4 service classes extend this with their entity type
- Provides shared `getAll()`, `findById()`, `delete()`, `count()` methods
- Holds `protected final FileRepository<T, String> repository`

### Polymorphism via RefundPolicy
Three implementations via `AbstractRefundPolicy` (which implements `RefundPolicy` interface):
- `NoRefundPolicy` - returns 0
- `FullRefundPolicy` - returns full amount
- `TieredRefundPolicy` - ≥7 days: 100%, 3-6 days: 50%, <3 days: 0%

### Pricing Formula
```
nights = DAYS.between(checkIn, checkOut)
subtotal = nights × pricePerNight
total = subtotal × (1 + vatRate)  // vatRate from settings.json, default 10%
```

### Data Persistence
- JSON files in `data/` directory: rooms.json, guests.json, bookings.json, settings.json
- Load on startup, save after every mutation
- Use Gson for JSON serialization

### macOS Compatibility
- JavaFX 21.0.2 is required for newer macOS versions (Darwin 25+)
- CSS uses custom variables (`-fx-base-bg`, `-fx-custom-border`) to avoid circular reference issues with JavaFX CSS lookup

## Implementation Phases

Follow `docs/AI_GUIDE.md` and the phase documents in order:
1. ✅ Setup & Models (enums, POJOs, RefundPolicy implementations) - COMPLETED
2. ✅ Persistence (Repository interface, FileRepository, JsonUtils, RepositoryFactory, Settings) - COMPLETED
3. ✅ Services (RoomService, GuestService, BookingService, InvoiceService) - COMPLETED
4. ✅ Utilities (DateUtil, MoneyUtil, ValidationUtil, AlertUtil) - COMPLETED
5. ✅ GUI Core (App.java, Dashboard, application.css) - COMPLETED
6. ✅ GUI Management (Room & Guest screens) - COMPLETED
7. ✅ GUI Booking (Booking, BookingList, Invoice screens) - COMPLETED
8. ✅ Testing (JUnit 5 tests) - COMPLETED

## Guest Portal Feature

Self-service portal for guests to browse rooms and make bookings.

### Architecture
- **State Management**: `GuestPortalState.java` - Static holder for cross-screen data flow
- **Service Enhancements**:
  - `BookingService.isRoomAvailableForDates()` - Date-based availability check
  - `RoomService.getAvailableRoomsForDates()` - Filter rooms by date availability
  - `GuestService.findOrCreateGuest()` - Lookup/create guest by email

### Portal Screens
| Screen | Controller | Purpose |
|--------|------------|---------|
| PortalSelection | PortalSelectionController | Entry point: Guest vs Staff |
| GuestPortalHome | GuestPortalHomeController | Guest welcome + Browse Rooms |
| GuestRoomBrowser | GuestRoomBrowserController | Date pickers + room cards |
| GuestBookingForm | GuestBookingFormController | Guest info + cost summary |
| GuestBookingConfirmation | GuestBookingConfirmationController | Booking success |

### User Flow
```
App Start → PortalSelection
              ├─ [Guest Portal] → GuestPortalHome → Browse → Book → Confirm
              └─ [Staff Portal] → Dashboard (management)
```

### CSS Classes
- `.portal-nav-button` - Large portal navigation buttons
- `.room-card` - Room listing cards
- `.confirmation-success` - Success styling

## Conventions

- Use `Optional<T>` instead of returning null
- Package: `com.example.hotel.*`
- FXML files paired with Controller classes in `gui/controllers/`
- CSS in `src/main/resources/styles/application.css`

Always update the phase document and CLAUDE.md after completion.
