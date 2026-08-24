# Ticket Booking System

A full-stack ticket booking platform for movies and concerts. The system allows customers to browse events, select seats visually, temporarily hold seats, confirm bookings, receive QR-code tickets by email, cancel bookings, and join category-based waitlists.

The system also provides separate roles for Customer, Organiser, and Admin.



## Features

### Customer

- Register and login
- Browse events
- View event details
- View visual seat map
- View seat availability
- Select seats
- Temporarily hold seats
- Confirm bookings
- View booking history
- Cancel bookings
- Join Premium or Standard waitlists
- Receive waitlist offers
- Accept waitlist offers
- Receive booking confirmation email
- Receive QR-code ticket

### Organiser

- Register and login
- Create events
- Select venue
- Set event date and time
- Set Premium ticket price
- Set Standard ticket price
- View created events
- View booking information
- View seat statistics

### Admin

- Login
- Add venues
- Manage venues
- Add seats
- Assign seat categories
- Manage venue seat layouts

---

## Technology Stack

### Frontend

- React
- Vite
- JavaScript
- CSS
- Fetch API

### Backend

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security
- REST APIs
- Maven

### Database

- MySQL

### Additional Technologies

- ZXing for QR-code generation
- Spring Mail for email delivery
- Spring Scheduling for automatic expiry processing

---

## Architecture

```text
                 React Frontend
                       |
                       | REST API
                       ↓
              Spring Boot Backend
                       |
          -------------------------
          |           |           |
       Security    Services   Controllers
          |           |
          |       Spring Data JPA
          |           |
          -----------↓-------------
                   MySQL
                       |
              Email / QR Service
```

---

## Project Structure

```text
ticket-booking-system/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ticketbooking/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   └── service/
│   │   │   └── resources/
│   │   ├── test/
│   │   └── ...
│   ├── pom.xml
│   └── mvnw.cmd
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── README.md
├── API-DOCUMENTATION.md
├── database-schema.sql
├── system-design.md
└── .env.example
```

---

## Database

The application uses MySQL.

Main tables:

```text
users
venues
seats
events
event_seats
bookings
waitlist
```

### Main relationships

```text
User
 ├── Bookings
 └── Waitlist Entries

Venue
 └── Seats

Event
 ├── Event Seats
 ├── Bookings
 └── Waitlist

Seat
 └── Event Seats
```

---

## Seat Status

Each event has its own event-seat records.

The main states are:

```text
AVAILABLE
HELD
BOOKED
```

The normal flow is:

```text
AVAILABLE
    ↓
HELD
    ↓
BOOKED
```

If the customer abandons checkout:

```text
HELD
 ↓
TTL expires
 ↓
AVAILABLE
```

---

## Seat Hold

When a customer selects an available seat, the backend places the seat in the `HELD` state.

The seat is temporarily unavailable to other customers.

A hold expiry time is stored for the seat.

If the customer confirms the booking before the expiry:

```text
HELD → BOOKED
```

If the hold expires:

```text
HELD → AVAILABLE
```

The backend uses scheduled processing to release expired holds.

---

## Concurrency Protection

The booking service uses database-level locking when accessing the event seat.

This prevents two customers from successfully booking the same seat simultaneously.

The backend verifies:

1. Seat exists
2. Seat is held
3. Correct user holds the seat
4. Hold has not expired
5. Seat can be converted to BOOKED

---

## Booking

After successful booking:

```text
Seat = BOOKED
Booking = CONFIRMED
```

The booking is stored in the database.

The customer can view the booking from booking history.

---

## Cancellation

When a customer cancels:

```text
CONFIRMED BOOKING
       ↓
CANCELLED
       ↓
SEAT AVAILABLE
```

The released seat is checked against the waitlist.

---

## Waitlist

Customers can join a waitlist for a specific category.

Example:

```text
Standard seats = 0
        ↓
Customer joins Standard Waitlist
```

The queue is ordered by the time customers joined.

When a booking is cancelled:

```text
Cancelled Seat
      ↓
Find first WAITING customer
      ↓
OFFERED
      ↓
Seat temporarily held
      ↓
Email notification
```

---

## Waitlist Offer

A waitlist customer receives a time-limited offer.

The current implementation uses a 10-minute offer period.

```text
WAITING
   ↓
OFFERED
   ↓
10-minute period
```

If accepted:

```text
OFFERED
   ↓
COMPLETED
   ↓
BOOKING CONFIRMED
```

If expired:

```text
OFFERED
   ↓
EXPIRED
   ↓
Seat released
   ↓
Next WAITING customer
```

---

## QR Code Ticket

After a confirmed booking, a booking reference is generated.

Example:

```text
BOOKING-53
```

The booking reference is encoded into a QR code.

The QR code is generated as a PNG image and attached to the confirmation email.

Flow:

```text
Booking
   ↓
Booking Reference
   ↓
QR Code
   ↓
PNG Image
   ↓
Email Attachment
   ↓
Customer
```

---

## Email

The system sends a booking confirmation email containing:

- Booking reference
- Customer name
- Event
- Seat
- Category
- Price
- Booking time
- QR-code ticket

The system also sends waitlist offer notifications.

---

## Local Setup

### Requirements

Install:

- Java JDK
- Node.js
- npm
- MySQL
- Git

---

## Backend Setup

Create the database:

```sql
CREATE DATABASE ticket_booking;
```

Open a terminal:

```cmd
cd backend
```

Build:

```cmd
.\mvnw.cmd clean package
```

Run:

```cmd
.\mvnw.cmd spring-boot:run
```

The backend normally runs on:

```text
http://localhost:8080
```

---

## Frontend Setup

Open another terminal:

```cmd
cd frontend
```

Install dependencies:

```cmd
npm install
```

Start the frontend:

```cmd
npm run dev
```

The frontend normally runs on:

```text
http://localhost:5173
```

---

## Configuration

Backend configuration is stored in:

```text
backend/src/main/resources/application.properties
```

The following configuration is required:

- MySQL connection
- Database username
- Database password
- Mail username
- Mail password
- Frontend URL

Never commit real passwords or secret credentials.

Use `.env.example` as a reference.

---

## Testing Flow

### Customer Booking

```text
Register
 ↓
Login
 ↓
Select Event
 ↓
Select Seat
 ↓
Hold Seat
 ↓
Confirm Booking
 ↓
Booking Created
 ↓
Email Received
 ↓
QR Code Received
```

### Waitlist

```text
Event Sold Out
 ↓
Join Waitlist
 ↓
Booking Cancelled
 ↓
Seat Offered
 ↓
Email Notification
 ↓
Accept Offer
 ↓
Booking Confirmed
```

### Expired Waitlist Offer

```text
Seat Offered
 ↓
10 Minutes Pass
 ↓
Offer Expires
 ↓
Seat Released
 ↓
Next Waitlist Customer
```

---

## API Documentation

See:

```text
API-DOCUMENTATION.md
```

---

## Database Schema

See:

```text
database-schema.sql
```

---

## System Design

See:

```text
system-design.md
```

---

## Project Deliverables

The project provides:

- Complete source code
- React frontend
- Spring Boot backend
- MySQL database
- Role-based authentication
- Visual seat map
- Seat holding
- Seat hold expiry
- Concurrency protection
- Booking
- Cancellation
- Waitlist
- Automatic waitlist allocation
- Time-limited waitlist offers
- QR-code generation
- Email notifications
- API documentation
- Database documentation
- System design documentation

---

## URLs

GitHub:

```text
<YOUR_GITHUB_URL>
```

Frontend:

```text
<YOUR_FRONTEND_URL>
```

Backend:

```text
<YOUR_BACKEND_URL>
```

Replace these placeholders after deployment.

---

## License

This project was developed as an academic full-stack ticket booking system project.
