# Ticket Booking System API Documentation

Base URL:

```text
http://localhost:8080
```

---

# Authentication APIs

## Register

```http
POST /api/auth/register
```

Creates a new customer or organiser account.

### Request

```json
{
  "name": "Test Customer",
  "email": "test@example.com",
  "password": "password123",
  "role": "CUSTOMER"
}
```

Allowed roles:

```text
CUSTOMER
ORGANISER
```

Admin accounts are managed separately.

---

## Login

```http
POST /api/auth/login
```

### Request

```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

### Response

The response contains authentication information including the user ID, name, email, role and token.

---

# Event APIs

## Get Events

```http
GET /api/events
```

Returns available events.

---

## Get Event

```http
GET /api/events/{eventId}
```

Returns details of a particular event.

---

## Create Event

```http
POST /api/events
```

Used by an organiser to create an event.

Typical information includes:

- Event title
- Description
- Date
- Time
- Premium price
- Standard price
- Venue

---

# Seat APIs

## Get Event Seats

```http
GET /api/events/{eventId}/seats
```

Returns the event's seat map.

Each event seat contains information such as:

- Seat number
- Category
- Status
- Event
- Hold information

---

## Hold Seat

```http
POST /api/seats/{eventSeatId}/hold
```

Temporarily holds an available seat for a customer.

The seat becomes:

```text
AVAILABLE → HELD
```

The hold expires automatically after the configured TTL.

---

# Booking APIs

## Confirm Booking

```http
POST /api/bookings/confirm
```

Confirms a held seat.

Typical parameters:

```text
eventSeatId
userId
```

The seat must:

- Exist
- Be HELD
- Belong to the requesting customer
- Have a valid hold

After successful confirmation:

```text
HELD → BOOKED
```

A booking record is created.

A confirmation email containing a QR-code ticket is sent.

---

## Cancel Booking

```http
POST /api/bookings/{bookingId}/cancel
```

Cancels a customer's booking.

The seat becomes available again.

If a waitlist exists for the seat category, the released seat is offered to the next waiting customer.

---

# Waitlist APIs

## Get Event Waitlist

```http
GET /api/waitlist/event/{eventId}
```

Returns waitlist entries for an event.

---

## Join Waitlist

```http
POST /api/waitlist/join
```

### Parameters

```text
eventId
userId
category
```

Example:

```text
POST /api/waitlist/join?eventId=1&userId=5&category=STANDARD
```

The customer is added to the selected category's queue.

---

## Accept Waitlist Offer

```http
POST /api/waitlist/{waitlistId}/accept
```

### Parameter

```text
userId
```

Example:

```text
POST /api/waitlist/12/accept?userId=5
```

The offer must still be active.

If accepted successfully:

```text
WAITLIST OFFERED
       ↓
COMPLETED
       ↓
BOOKING CONFIRMED
```

---

# Venue APIs

## Get Venues

```http
GET /api/venues
```

Returns venues.

---

## Create Venue

```http
POST /api/venues
```

Creates a new venue.

---

## Add Seat

```http
POST /api/venues/{venueId}/seats
```

Adds a seat to a venue.

Typical fields:

```text
seatNumber
category
```

Categories:

```text
PREMIUM
STANDARD
```

---

# Booking History

The customer can retrieve their bookings through the booking-related API exposed by the backend.

The booking information includes:

- Booking ID
- Event
- Seat
- Category
- Price
- Status
- Booking time

---

# Important Status Values

## Seat Status

```text
AVAILABLE
HELD
BOOKED
```

## Booking Status

```text
CONFIRMED
CANCELLED
```

## Waitlist Status

```text
WAITING
OFFERED
COMPLETED
EXPIRED
```

---

# Error Handling

The backend returns an error message when an operation cannot be completed.

Examples:

```text
Event not found
User not found
Seat is not currently held
Seat is held by another user
Seat hold has expired
Booking not found
You can cancel only your own booking
You are already on the waitlist
No active offer exists
Waitlist offer has expired
```

---

# Authentication

The frontend stores the authentication token after login and uses it for authenticated operations.

Roles:

```text
CUSTOMER
ORGANISER
ADMIN
```

Role-specific functionality is controlled by the backend and frontend.

---

# Booking Flow

```text
Customer
   ↓
GET Events
   ↓
Select Event
   ↓
GET Event Seats
   ↓
Hold Seat
   ↓
Confirm Booking
   ↓
Booking Created
   ↓
QR Code Generated
   ↓
Email Sent
```

---

# Waitlist Flow

```text
Event Sold Out
       ↓
Join Waitlist
       ↓
Booking Cancelled
       ↓
Find First WAITING Customer
       ↓
OFFERED
       ↓
10-Minute Offer
       ↓
Accept
       ↓
BOOKING CONFIRMED
```

If the offer expires:

```text
OFFERED
   ↓
EXPIRED
   ↓
Seat Released
   ↓
Next WAITING Customer
```