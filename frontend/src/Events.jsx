import { useEffect, useState } from "react";
import "./Events.css";

function Events({ onLogout }) {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [seats, setSeats] = useState([]);
  const [message, setMessage] = useState("Loading events...");
  const [selectedSeatId, setSelectedSeatId] = useState(null);
  const [booking, setBooking] = useState(null);

  const [showBookings, setShowBookings] = useState(false);
  const [bookings, setBookings] = useState([]);

  // WAITLIST
  const [waitlistEntries, setWaitlistEntries] = useState([]);

  // =========================
  // LOAD EVENTS
  // =========================

  useEffect(() => {
    loadEvents();
  }, []);

  const loadEvents = async () => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch("/api/events", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        setMessage("Unable to load events");
        return;
      }

      const data = await response.json();

      setEvents(data);
      setMessage("");
    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // LOAD BOOKINGS
  // =========================

  const loadBookings = async () => {
    try {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      const response = await fetch(
        `/api/bookings/user/${userId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();

        setMessage(
          errorText || "Unable to load bookings"
        );

        return;
      }

      const data = await response.json();

      setBookings(data);
      setShowBookings(true);

      setSelectedEvent(null);
      setSeats([]);
      setSelectedSeatId(null);
      setBooking(null);
      setMessage("");
    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // CANCEL BOOKING
  // =========================

  const cancelBooking = async (bookingId) => {
    const confirmCancel = window.confirm(
      "Are you sure you want to cancel this booking?"
    );

    if (!confirmCancel) {
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      const response = await fetch(
        `/api/bookings/${bookingId}/cancel?userId=${userId}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();

        setMessage(
          errorText || "Unable to cancel booking"
        );

        return;
      }

      setMessage(
        "Booking cancelled successfully!"
      );

      await loadBookings();
    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // LOAD SEATS
  // =========================

  const loadSeats = async (eventId) => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `/api/events/${eventId}/seats`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        setMessage("Unable to load seats");
        return;
      }

      const data = await response.json();

      setSeats(data);
    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // LOAD WAITLIST
  // =========================

  const loadWaitlist = async (eventId) => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `/api/waitlist/event/${eventId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        setWaitlistEntries([]);
        return;
      }

      const data = await response.json();

      setWaitlistEntries(data);
    } catch (error) {
      console.error("Waitlist error:", error);
      setWaitlistEntries([]);
    }
  };

  // =====================================================
  // REAL-TIME SEAT MAP + WAITLIST REFRESH
  // =====================================================

  useEffect(() => {
    if (!selectedEvent) {
      return;
    }

    const refreshSeatMap = async () => {
      await loadSeats(selectedEvent.id);
      await loadWaitlist(selectedEvent.id);
    };

    const intervalId = setInterval(
      refreshSeatMap,
      3000
    );

    return () => {
      clearInterval(intervalId);
    };
  }, [selectedEvent]);

  // =========================
  // VIEW SEATS
  // =========================

  const viewSeats = async (event) => {
    setSelectedEvent(event);
    setSelectedSeatId(null);
    setBooking(null);
    setMessage("");
    setWaitlistEntries([]);

    await loadSeats(event.id);
    await loadWaitlist(event.id);
  };

  // =========================
  // GET SEAT PRICE
  // =========================

  const getSeatPrice = (seat) => {
    if (!selectedEvent || !seat?.seat) {
      return 0;
    }

    if (seat.seat.category === "PREMIUM") {
      return selectedEvent.premiumPrice;
    }

    return selectedEvent.standardPrice;
  };

  // =========================
  // GET SELECTED SEAT
  // =========================

  const getSelectedSeat = () => {
    return seats.find(
      (seat) => seat.id === selectedSeatId
    );
  };

  // =========================
  // HOLD SEAT
  // =========================

  const holdSeat = async (seat) => {
    if (seat.status !== "AVAILABLE") {
      setMessage(
        `Seat is already ${seat.status}`
      );

      return;
    }

    try {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      const response = await fetch(
        `/api/events/${selectedEvent.id}/seats/${seat.id}/hold?userId=${userId}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();

        setMessage(
          errorText || "Unable to hold seat"
        );

        return;
      }

      await response.json();

      setSelectedSeatId(seat.id);

      setMessage(
        "Seat held successfully. Confirm your booking within 10 minutes."
      );

      await loadSeats(selectedEvent.id);
    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // CONFIRM BOOKING
  // =========================

  const confirmBooking = async () => {
  if (!selectedSeatId) {
    setMessage("Please select and hold a seat first.");
    return;
  }

  try {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    // IMPORTANT
    if (!token) {
      setMessage("Please login again.");
      return;
    }

    if (!userId || userId === "null" || userId === "undefined") {
      setMessage("User ID missing. Please logout and login again.");
      console.error("Missing userId:", userId);
      return;
    }

    console.log("Confirm booking:", {
      eventSeatId: selectedSeatId,
      userId: userId
    });

    const response = await fetch(
      `/api/bookings/confirm?eventSeatId=${selectedSeatId}&userId=${userId}`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      }
    );

    const responseText = await response.text();

    console.log("Confirm response:", response.status, responseText);

    if (!response.ok) {
      setMessage(
        responseText || `Booking failed (${response.status})`
      );
      return;
    }

    const data = JSON.parse(responseText);

    console.log("BOOKING SUCCESS:", data);

    setBooking(data);
    setSelectedSeatId(null);
    setMessage("Booking confirmed successfully!");

    await loadSeats(selectedEvent.id);

  } catch (error) {
    console.error("CONFIRM BOOKING ERROR:", error);
    setMessage("Unable to connect to server");
  }
};

  // =========================
  // JOIN WAITLIST
  // =========================

  const joinWaitlist = async (category) => {
    if (!selectedEvent) {
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      const response = await fetch(
        `/api/waitlist/join?eventId=${selectedEvent.id}&userId=${userId}&category=${category}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const responseText = await response.text();

      if (!response.ok) {
        setMessage(
          responseText ||
            "Unable to join waitlist"
        );

        return;
      }

      setMessage(
        `Successfully joined the ${category} waitlist!`
      );

      await loadWaitlist(selectedEvent.id);
    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // ACCEPT WAITLIST OFFER
  // =========================

  const acceptWaitlistOffer = async (
    waitlistId
  ) => {
    try {
      const token = localStorage.getItem("token");
      const userId = localStorage.getItem("userId");

      const response = await fetch(
        `/api/waitlist/${waitlistId}/accept?userId=${userId}`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const responseText = await response.text();

      if (!response.ok) {
        setMessage(
          responseText ||
            "Unable to accept seat offer"
        );

        return;
      }

      setMessage(
        "Seat offer accepted successfully!"
      );

      await loadSeats(selectedEvent.id);
      await loadWaitlist(selectedEvent.id);
    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // BACK TO EVENTS
  // =========================

  const backToEvents = () => {
    setSelectedEvent(null);
    setSeats([]);
    setSelectedSeatId(null);
    setBooking(null);
    setWaitlistEntries([]);
    setMessage("");
  };

  // =====================================================
  // MY BOOKINGS PAGE
  // =====================================================

  if (showBookings) {
    return (
      <div className="events-container">

        <div className="events-header">

          <div>
            <h1>Ticket Booking</h1>
            <p>My Bookings</p>
          </div>

          <div>

            <button
              className="back-button"
              onClick={() => {
                setShowBookings(false);
                setMessage("");
              }}
            >
              ← Back
            </button>

            <button
              className="logout-button"
              onClick={onLogout}
            >
              Logout
            </button>

          </div>

        </div>

        <div className="events-content">

          <h2>My Bookings</h2>

          {message && (
            <p className="events-message">
              {message}
            </p>
          )}

          {bookings.length === 0 ? (
            <p>No bookings found.</p>
          ) : (
            <div className="booking-grid">

              {bookings.map((booking) => (

                <div
                  className="booking-card"
                  key={booking.bookingId}
                >

                  <h3>
                    {booking.eventTitle}
                  </h3>

                  <p>
                    <strong>
                      Booking ID:
                    </strong>{" "}
                    {booking.bookingId}
                  </p>

                  <p>
                    <strong>
                      Seat:
                    </strong>{" "}
                    {booking.seatNumber || "-"}
                  </p>

                  <p>
                    <strong>
                      Category:
                    </strong>{" "}
                    {booking.category || "-"}
                  </p>

                  <p>
                    <strong>
                      Status:
                    </strong>{" "}
                    {booking.status}
                  </p>

                  <p>
                    <strong>
                      Price:
                    </strong>{" "}
                    ₹{booking.price}
                  </p>

                  <p>
                    <strong>
                      Booked At:
                    </strong>{" "}
                    {booking.bookedAt
                      ? new Date(
                          booking.bookedAt
                        ).toLocaleString()
                      : "-"}
                  </p>

                  {booking.status ===
                    "CONFIRMED" && (

                    <button
                      className="cancel-button"
                      onClick={() =>
                        cancelBooking(
                          booking.bookingId
                        )
                      }
                    >
                      Cancel Booking
                    </button>

                  )}

                </div>

              ))}

            </div>
          )}

        </div>

      </div>
    );
  }

  // =====================================================
  // SEAT PAGE
  // =====================================================

  if (selectedEvent) {

    const selectedSeat =
      getSelectedSeat();

    const currentUserId = Number(
      localStorage.getItem("userId")
    );

    // =========================
    // CATEGORY INFORMATION
    // =========================

    const premiumSeats = seats.filter(
      (seat) =>
        seat.seat?.category === "PREMIUM"
    );

    const standardSeats = seats.filter(
      (seat) =>
        seat.seat?.category === "STANDARD"
    );

    const premiumAvailable =
      premiumSeats.filter(
        (seat) =>
          seat.status === "AVAILABLE"
      ).length;

    const standardAvailable =
      standardSeats.filter(
        (seat) =>
          seat.status === "AVAILABLE"
      ).length;

    const myPremiumWaitlist =
  waitlistEntries.find(
    (entry) =>
      entry.category === "PREMIUM" &&
      entry.user?.id === currentUserId &&
      (
        entry.status === "WAITING" ||
        entry.status === "OFFERED"
      )
  );

const myStandardWaitlist =
  waitlistEntries.find(
    (entry) =>
      entry.category === "STANDARD" &&
      entry.user?.id === currentUserId &&
      (
        entry.status === "WAITING" ||
        entry.status === "OFFERED"
      )
  );

    const premiumOffer =
      waitlistEntries.find(
        (entry) =>
          entry.category === "PREMIUM" &&
          entry.status === "OFFERED" &&
          entry.user?.id === currentUserId
      );

    const standardOffer =
      waitlistEntries.find(
        (entry) =>
          entry.category === "STANDARD" &&
          entry.status === "OFFERED" &&
          entry.user?.id === currentUserId
      );

    return (
      <div className="events-container">

        {/* =========================
            HEADER
        ========================= */}

        <div className="events-header">

          <div>
            <h1>Ticket Booking</h1>
            <p>{selectedEvent.title}</p>
          </div>

          <div>

            <button
              className="bookings-button"
              onClick={loadBookings}
            >
              My Bookings
            </button>

            <button
              className="logout-button"
              onClick={onLogout}
            >
              Logout
            </button>

          </div>

        </div>

        <div className="events-content">

          {/* =========================
              BACK BUTTON
          ========================= */}

          <button
            className="back-button"
            onClick={backToEvents}
          >
            ← Back to Events
          </button>

          <h2>
            Select Your Seat
          </h2>

          {/* =========================
              PRICE INFORMATION
          ========================= */}

          <div className="price-info">

            <span>
              🟡 Premium: ₹
              {selectedEvent.premiumPrice}
            </span>

            <span>
              🟢 Standard: ₹
              {selectedEvent.standardPrice}
            </span>

          </div>

          {/* =========================
              MESSAGE
          ========================= */}

          {message && (
            <p className="events-message">
              {message}
            </p>
          )}

          {/* =========================
              SEAT GRID
          ========================= */}

          <div className="seat-grid">

            {seats.map((seat) => {

              const category =
                seat.seat?.category ||
                "STANDARD";

              const price =
                getSeatPrice(seat);

              const seatNumber =
                seat.seat?.seatNumber ||
                seat.id;

              return (

                <button
                  key={seat.id}
                  className={`seat ${seat.status.toLowerCase()} ${category.toLowerCase()} ${
                    selectedSeatId === seat.id
                      ? "selected-seat"
                      : ""
                  }`}
                  onClick={() =>
                    holdSeat(seat)
                  }
                  disabled={
                    seat.status !==
                    "AVAILABLE"
                  }
                >

                  <strong>
                    {seatNumber}
                  </strong>

                  <span>
                    {category}
                  </span>

                  <span>
                    ₹{price}
                  </span>

                  <small>
                    {seat.status}
                  </small>

                </button>

              );
            })}

          </div>

          {/* =====================================================
              WAITLIST SECTION
          ===================================================== */}

          <div className="waitlist-section">

            <h3>
              Waitlist
            </h3>

            {/* =========================
                PREMIUM WAITLIST
            ========================= */}

            <div className="waitlist-card">

              <h4>
                Premium Waitlist
              </h4>

              <p>
                Available seats:{" "}
                <strong>
                  {premiumAvailable}
                </strong>
              </p>

              {premiumOffer ? (

                <div>

                  <p>
                    🎟️ A Premium seat has
                    been offered to you!
                  </p>

                  <p>
                    Offer expires at:{" "}
                    {premiumOffer.offerExpiresAt
                      ? new Date(
                          premiumOffer.offerExpiresAt
                        ).toLocaleString()
                      : "-"}
                  </p>

                  <button
                    className="confirm-button"
                    onClick={() =>
                      acceptWaitlistOffer(
                        premiumOffer.id
                      )
                    }
                  >
                    Accept Seat Offer
                  </button>

                </div>

              ) : myPremiumWaitlist ? (

                <div>

                  <p>
                    ✓ You are on the
                    Premium waitlist
                  </p>

                  <p>
                    Status:{" "}
                    <strong>
                      {myPremiumWaitlist.status}
                    </strong>
                  </p>

                </div>

              ) : premiumAvailable ===
                0 ? (

                <button
                  className="book-button"
                  onClick={() =>
                    joinWaitlist(
                      "PREMIUM"
                    )
                  }
                >
                  Join Premium Waitlist
                </button>

              ) : (

                <p>
                  Premium seats are
                  currently available.
                </p>

              )}

            </div>

            {/* =========================
                STANDARD WAITLIST
            ========================= */}

            <div className="waitlist-card">

              <h4>
                Standard Waitlist
              </h4>

              <p>
                Available seats:{" "}
                <strong>
                  {standardAvailable}
                </strong>
              </p>

              {standardOffer ? (

                <div>

                  <p>
                    🎟️ A Standard seat has
                    been offered to you!
                  </p>

                  <p>
                    Offer expires at:{" "}
                    {standardOffer.offerExpiresAt
                      ? new Date(
                          standardOffer.offerExpiresAt
                        ).toLocaleString()
                      : "-"}
                  </p>

                  <button
                    className="confirm-button"
                    onClick={() =>
                      acceptWaitlistOffer(
                        standardOffer.id
                      )
                    }
                  >
                    Accept Seat Offer
                  </button>

                </div>

              ) : myStandardWaitlist ? (

                <div>

                  <p>
                    ✓ You are on the
                    Standard waitlist
                  </p>

                  <p>
                    Status:{" "}
                    <strong>
                      {myStandardWaitlist.status}
                    </strong>
                  </p>

                </div>

              ) : standardAvailable ===
                0 ? (

                <button
                  className="book-button"
                  onClick={() =>
                    joinWaitlist(
                      "STANDARD"
                    )
                  }
                >
                  Join Standard Waitlist
                </button>

              ) : (

                <p>
                  Standard seats are
                  currently available.
                </p>

              )}

            </div>

          </div>

          {/* =====================================================
              SELECTED SEAT
          ===================================================== */}

          {selectedSeat && !booking && (

            <div className="confirmation-box">

              <h3>
                Seat Selected
              </h3>

              <p>
                <strong>
                  Seat:
                </strong>{" "}
                {selectedSeat.seat?.seatNumber}
              </p>

              <p>
                <strong>
                  Category:
                </strong>{" "}
                {selectedSeat.seat?.category}
              </p>

              <p>
                <strong>
                  Price:
                </strong>{" "}
                ₹{getSeatPrice(selectedSeat)}
              </p>

              <button
                className="confirm-button"
                onClick={confirmBooking}
              >
                Confirm Booking
              </button>

            </div>

          )}

          {/* =====================================================
              BOOKING SUCCESS
          ===================================================== */}

          {booking && (

            <div className="booking-success">

              <div className="ticket-icon">
                🎟️
              </div>

              <h2>
                Booking Confirmed!
              </h2>

              <p>
                Your ticket has been booked
                successfully.
              </p>

              <div className="booking-details">

                <div className="booking-detail-row">

                  <span>
                    Event
                  </span>

                  <strong>
                    {booking.eventTitle ||
                      selectedEvent.title}
                  </strong>

                </div>

                <div className="booking-detail-row">

                  <span>
                    Booking ID
                  </span>

                  <strong>
                    {booking.bookingId}
                  </strong>

                </div>

                <div className="booking-detail-row">

                  <span>
                    Seat
                  </span>

                  <strong>
                    {booking.seatNumber ||
                      "-"}
                  </strong>

                </div>

                <div className="booking-detail-row">

                  <span>
                    Category
                  </span>

                  <strong>
                    {booking.category ||
                      "-"}
                  </strong>

                </div>

                <div className="booking-detail-row">

                  <span>
                    Price
                  </span>

                  <strong>
                    ₹{booking.price}
                  </strong>

                </div>

                <div className="booking-detail-row">

                  <span>
                    Status
                  </span>

                  <strong>
                    {booking.status}
                  </strong>

                </div>

                <div className="booking-detail-row">

                  <span>
                    Booked At
                  </span>

                  <strong>
                    {booking.bookedAt
                      ? new Date(
                          booking.bookedAt
                        ).toLocaleString()
                      : "-"}
                  </strong>

                </div>

              </div>

              {booking.qrCode && (

                <div
                  className="qr-section"
                  style={{
                    marginTop: "24px",
                    padding: "20px",
                    textAlign: "center",
                    borderRadius: "12px",
                    background: "#f8f8f8"
                  }}
                >

                  <h3>Ticket QR Code</h3>

                  <img
                    src={booking.qrCode}
                    alt={`QR Code for Booking ${booking.bookingId}`}
                    style={{
                      width: "300px",
                      height: "300px",
                      maxWidth: "100%",
                      display: "block",
                      margin: "15px auto"
                    }}
                  />

                  <p>Scan this QR code at the venue.</p>

                  <a
                    href={booking.qrCode}
                    download={`BOOKING-${booking.bookingId}.png`}
                    style={{
                      display: "inline-block",
                      padding: "10px 18px",
                      borderRadius: "8px",
                      background: "#222",
                      color: "white",
                      textDecoration: "none",
                      cursor: "pointer"
                    }}
                  >
                    Download QR
                  </a>

                </div>

              )}

              <button
                className="book-button"
                onClick={() => {
                  setBooking(null);
                  setSelectedSeatId(null);
                  setMessage("");
                }}
              >
                Book Another Seat
              </button>

            </div>

          )}

        </div>

      </div>
    );
  }

  // =====================================================
  // EVENTS DASHBOARD
  // =====================================================

  return (

    <div className="events-container">

      {/* =========================
          HEADER
      ========================= */}

      <div className="events-header">

        <div>

          <h1>
            Ticket Booking
          </h1>

          <p>
            Choose an event and book
            your seats
          </p>

        </div>

        <div>

          <button
            className="bookings-button"
            onClick={loadBookings}
          >
            My Bookings
          </button>

          <button
            className="logout-button"
            onClick={onLogout}
          >
            Logout
          </button>

        </div>

      </div>

      {/* =========================
          EVENTS
      ========================= */}

      <div className="events-content">

        <h2>
          Available Events
        </h2>

        {message && (
          <p className="events-message">
            {message}
          </p>
        )}

        <div className="event-grid">

          {events.map((event) => (

            <div
              className="event-card"
              key={event.id}
            >

              <h3>
                {event.title}
              </h3>

              <p>
                {event.description}
              </p>

              <p>
                <strong>
                  Date:
                </strong>{" "}
                {event.eventDate}
              </p>

              <p>
                <strong>
                  Time:
                </strong>{" "}
                {event.eventTime}
              </p>

              <p>
                <strong>
                  Premium:
                </strong>{" "}
                ₹{event.premiumPrice}
              </p>

              <p>
                <strong>
                  Standard:
                </strong>{" "}
                ₹{event.standardPrice}
              </p>

              {event.venueName && (

                <p>

                  <strong>
                    Venue:
                  </strong>{" "}

                  {event.venueName}

                </p>

              )}

              {event.venueLocation && (

                <p>

                  <strong>
                    Location:
                  </strong>{" "}

                  {event.venueLocation}

                </p>

              )}

              <button
                className="book-button"
                onClick={() =>
                  viewSeats(event)
                }
              >
                View Seats
              </button>

            </div>

          ))}

        </div>

      </div>

    </div>
  );
}

export default Events;