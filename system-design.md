# Ticket Booking System - System Design

## 1. Seat Hold and TTL Mechanism

The system maintains a separate event-seat record for each seat belonging to an event. Each event seat has a status such as AVAILABLE, HELD or BOOKED.

When a customer selects an available seat, the backend temporarily places the seat in the HELD state. The seat is associated with the customer and an expiration timestamp is stored.

The normal state transition is:

```text
AVAILABLE → HELD → BOOKED
```

If the customer completes the booking before the hold expires, the seat becomes BOOKED and a booking record is created.

If the customer abandons checkout, the hold eventually expires:

```text
HELD → AVAILABLE
```

The Spring Boot application uses scheduling to periodically identify expired holds and release the corresponding seats. This prevents abandoned checkouts from permanently blocking seats.

---

## 2. Concurrency Protection

Concurrency is important because multiple customers may attempt to select the same seat at approximately the same time.

The booking service uses database-level locking when retrieving the event seat for booking. The service then verifies the current state of the seat before confirming the booking.

The booking process checks:

1. The seat exists.
2. The seat is currently HELD.
3. The seat is held by the requesting customer.
4. The hold has not expired.

Only after these checks does the service change the seat state to BOOKED.

The transaction ensures that the seat state change and booking creation are performed as part of the booking operation. This prevents two simultaneous customers from successfully booking the same seat.

---

## 3. Waitlist Auto-Assignment

When all seats of a category are unavailable, a customer can join the waitlist for that category.

Waitlist entries are maintained in queue order based on their joining time.

When a customer cancels a confirmed booking, the released event seat becomes available to the waitlist mechanism.

The system searches for the first customer whose waitlist status is WAITING and whose requested category matches the released seat.

The selected customer receives an OFFERED status. The seat is temporarily held for that customer and an offer expiration timestamp is stored.

The flow is:

```text
Booking Cancelled
       ↓
Seat Released
       ↓
Find First WAITING Customer
       ↓
OFFERED
       ↓
Seat Held
       ↓
Email Notification
```

---

## 4. Time-Limited Offer Handling

The waitlist offer is valid for a limited period of 10 minutes.

If the customer accepts the offer within the time limit, the seat becomes BOOKED and the waitlist entry becomes COMPLETED. A normal booking record is created and the customer receives the normal booking confirmation email containing the QR-code ticket.

If the customer does not accept within the allowed period, the offer becomes EXPIRED. The seat is released and the system attempts to offer it to the next WAITING customer.

The expiry process is also handled automatically by a scheduled backend task.

The flow is:

```text
WAITING
   ↓
OFFERED
   ↓
10 Minutes
   ↓
Accepted → COMPLETED → BOOKED
```

or:

```text
OFFERED
   ↓
Expired
   ↓
EXPIRED
   ↓
Next WAITING Customer
```

---

## 5. QR Code and Email

After a confirmed booking, the system creates a booking reference in the format:

```text
BOOKING-<bookingId>
```

The reference is encoded into a QR code using ZXing. The generated QR code is converted into a PNG image and attached to the booking confirmation email.

Therefore the customer receives both the booking information and the QR-code ticket through email.

---

## 6. Overall Architecture

The frontend is implemented using React and communicates with the Spring Boot backend through REST APIs.

The backend contains controllers, services, repositories and JPA entities. MySQL stores users, venues, seats, events, event seats, bookings and waitlist entries.

The overall flow is:

```text
React Frontend
      ↓
REST API
      ↓
Spring Boot
      ↓
Service Layer
      ↓
Spring Data JPA
      ↓
MySQL
```

Email and QR-code generation are handled by dedicated backend services.