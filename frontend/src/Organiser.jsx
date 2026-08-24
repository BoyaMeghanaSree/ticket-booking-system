import { useEffect, useState } from "react";
import "./Events.css";

function Organiser({ onLogout }) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [eventDate, setEventDate] = useState("");
  const [eventTime, setEventTime] = useState("");
  const [premiumPrice, setPremiumPrice] = useState("");
  const [standardPrice, setStandardPrice] = useState("");
  const [venueId, setVenueId] = useState("");

  const [venues, setVenues] = useState([]);
  const [events, setEvents] = useState([]);

  const [message, setMessage] = useState("");
  const [loadingEvents, setLoadingEvents] = useState(true);

  const [selectedEvent, setSelectedEvent] = useState(null);
  const [eventBookings, setEventBookings] = useState([]);
  const [loadingBookings, setLoadingBookings] = useState(false);

  // =========================
  // LOAD DATA
  // =========================

  useEffect(() => {
    loadVenues();
    loadOrganiserEvents();
  }, []);

  // =========================
  // LOAD VENUES
  // =========================

  const loadVenues = async () => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch("/api/venues", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        setMessage("Unable to load venues");
        return;
      }

      const data = await response.json();
      setVenues(data);

    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // LOAD ORGANISER EVENTS
  // =========================

  const loadOrganiserEvents = async () => {
    try {
      const token = localStorage.getItem("token");
      const organiserId = localStorage.getItem("userId");

      const response = await fetch(
        `/api/events/organiser/${organiserId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        setMessage("Unable to load your events");
        return;
      }

      const data = await response.json();
      setEvents(data);

    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");

    } finally {
      setLoadingEvents(false);
    }
  };

  // =========================
  // CREATE EVENT
  // =========================

  const createEvent = async (e) => {
    e.preventDefault();

    try {
      const token = localStorage.getItem("token");
      const organiserId = localStorage.getItem("userId");

      const response = await fetch("/api/events", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },

        body: JSON.stringify({
          title,
          description,
          eventDate,
          eventTime,
          premiumPrice: Number(premiumPrice),
          standardPrice: Number(standardPrice),
          venueId: Number(venueId),
          organiserId: Number(organiserId),
        }),
      });

      const data = await response.text();

      if (response.ok) {

        setMessage(
          `Event created successfully! ${data}`
        );

        setTitle("");
        setDescription("");
        setEventDate("");
        setEventTime("");
        setPremiumPrice("");
        setStandardPrice("");
        setVenueId("");

        await loadOrganiserEvents();

      } else {

        setMessage(
          data || "Unable to create event"
        );
      }

    } catch (error) {

      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // GET SEAT STATISTICS
  // =========================

  const getSeatStatistics = async (eventId) => {

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
        return {
          total: 0,
          available: 0,
          booked: 0,
          held: 0,
        };
      }

      const seats = await response.json();

      return {
        total: seats.length,

        available: seats.filter(
          (seat) =>
            seat.status === "AVAILABLE"
        ).length,

        booked: seats.filter(
          (seat) =>
            seat.status === "BOOKED"
        ).length,

        held: seats.filter(
          (seat) =>
            seat.status === "HELD"
        ).length,
      };

    } catch (error) {

      console.error(error);

      return {
        total: 0,
        available: 0,
        booked: 0,
        held: 0,
      };
    }
  };

  // =========================
  // VIEW BOOKINGS
  // =========================

  const viewBookings = async (event) => {

    try {

      setSelectedEvent(event);
      setEventBookings([]);
      setLoadingBookings(true);
      setMessage("");

      const token =
        localStorage.getItem("token");

      const organiserId =
        localStorage.getItem("userId");

      const response = await fetch(
        `/api/bookings/organiser/${organiserId}/event/${event.id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {

        const errorText =
          await response.text();

        setMessage(
          errorText ||
          "Unable to load bookings"
        );

        return;
      }

      const data =
        await response.json();

      setEventBookings(data);

    } catch (error) {

      console.error(error);

      setMessage(
        "Unable to connect to server"
      );

    } finally {

      setLoadingBookings(false);
    }
  };

  // =========================
  // BACK TO EVENTS
  // =========================

  const backToEvents = () => {

    setSelectedEvent(null);
    setEventBookings([]);
    setMessage("");
  };

  // =========================
  // EVENT CARD
  // =========================

  const EventCard = ({ event }) => {

    const [stats, setStats] = useState({
      total: 0,
      available: 0,
      booked: 0,
      held: 0,
    });

    const [loading, setLoading] =
      useState(true);

    useEffect(() => {

      const loadStats = async () => {

        const result =
          await getSeatStatistics(
            event.id
          );

        setStats(result);
        setLoading(false);
      };

      loadStats();

    }, [event.id]);

    return (
      <div className="event-card">

        <h3>{event.title}</h3>

        <p>
          {event.description}
        </p>

        <p>
          <strong>Date:</strong>{" "}
          {event.eventDate}
        </p>

        <p>
          <strong>Time:</strong>{" "}
          {event.eventTime}
        </p>

        <p>
          <strong>Premium:</strong>{" "}
          ₹{event.premiumPrice}
        </p>

        <p>
          <strong>Standard:</strong>{" "}
          ₹{event.standardPrice}
        </p>

        <p>
          <strong>Venue:</strong>{" "}
          {event.venueName}
        </p>

        <p>
          <strong>Location:</strong>{" "}
          {event.venueLocation}
        </p>

        <div className="organiser-stats">

          <div className="stat-box">
            <strong>
              {loading ? "..." : stats.total}
            </strong>
            <span>Total Seats</span>
          </div>

          <div className="stat-box">
            <strong>
              {loading
                ? "..."
                : stats.available}
            </strong>
            <span>Available</span>
          </div>

          <div className="stat-box">
            <strong>
              {loading
                ? "..."
                : stats.booked}
            </strong>
            <span>Booked</span>
          </div>

          <div className="stat-box">
            <strong>
              {loading
                ? "..."
                : stats.held}
            </strong>
            <span>Held</span>
          </div>

        </div>

        <button
          className="book-button"
          onClick={() =>
            viewBookings(event)
          }
        >
          View Bookings
        </button>

      </div>
    );
  };

  // =========================
  // BOOKING DETAILS PAGE
  // =========================

  if (selectedEvent) {

    return (
      <div className="events-container">

        <div className="events-header">

          <div>

            <h1>
              Ticket Booking
            </h1>

            <p>
              {selectedEvent.title}
            </p>

          </div>

          <button
            className="logout-button"
            onClick={onLogout}
          >
            Logout
          </button>

        </div>

        <div className="events-content">

          <button
            className="back-button"
            onClick={backToEvents}
          >
            ← Back to My Events
          </button>

          <h2>
            Bookings for{" "}
            {selectedEvent.title}
          </h2>

          {message && (
            <p className="events-message">
              {message}
            </p>
          )}

          {loadingBookings ? (

            <p className="events-message">
              Loading bookings...
            </p>

          ) : eventBookings.length === 0 ? (

            <div className="confirmation-box">

              <h3>
                No Bookings Yet
              </h3>

              <p>
                No customers have booked
                seats for this event yet.
              </p>

            </div>

          ) : (

            <div className="booking-grid">

              {eventBookings.map(
                (booking) => (

                  <div
                    className="booking-card"
                    key={booking.bookingId}
                  >

                    <h3>
                      Booking #
                      {booking.bookingId}
                    </h3>

                    <p>
                      <strong>
                        Customer:
                      </strong>{" "}
                      {booking.customerName}
                    </p>

                    <p>
                      <strong>
                        Email:
                      </strong>{" "}
                      {booking.customerEmail}
                    </p>

                    <p>
                      <strong>
                        Event:
                      </strong>{" "}
                      {booking.eventTitle}
                    </p>

                    <p>
                      <strong>
                        Seat:
                      </strong>{" "}
                      {booking.seatNumber}
                    </p>

                    <p>
                      <strong>
                        Category:
                      </strong>{" "}
                      {booking.category}
                    </p>

                    <p>
                      <strong>
                        Price:
                      </strong>{" "}
                      ₹{booking.price}
                    </p>

                    <p>
                      <strong>
                        Status:
                      </strong>{" "}
                      {booking.status}
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

                  </div>

                )
              )}

            </div>
          )}

        </div>

      </div>
    );
  }

  // =========================
  // MAIN ORGANISER DASHBOARD
  // =========================

  return (
    <div className="events-container">

      <div className="events-header">

        <div>

          <h1>
            Ticket Booking
          </h1>

          <p>
            Organiser Dashboard
          </p>

        </div>

        <button
          className="logout-button"
          onClick={onLogout}
        >
          Logout
        </button>

      </div>

      <div className="events-content">

        {/* CREATE EVENT */}

        <div className="confirmation-box">

          <h2>
            Create New Event
          </h2>

          <form onSubmit={createEvent}>

            <label>
              Event Title
            </label>

            <input
              type="text"
              value={title}
              onChange={(e) =>
                setTitle(e.target.value)
              }
              placeholder="Enter event title"
              required
            />

            <label>
              Description
            </label>

            <textarea
              value={description}
              onChange={(e) =>
                setDescription(
                  e.target.value
                )
              }
              placeholder="Enter event description"
              required
            />

            <label>
              Date
            </label>

            <input
              type="date"
              value={eventDate}
              onChange={(e) =>
                setEventDate(
                  e.target.value
                )
              }
              required
            />

            <label>
              Time
            </label>

            <input
              type="time"
              value={eventTime}
              onChange={(e) =>
                setEventTime(
                  e.target.value
                )
              }
              required
            />

            <label>
              Premium Price
            </label>

            <input
              type="number"
              value={premiumPrice}
              onChange={(e) =>
                setPremiumPrice(
                  e.target.value
                )
              }
              placeholder="Enter premium price"
              min="0"
              required
            />

            <label>
              Standard Price
            </label>

            <input
              type="number"
              value={standardPrice}
              onChange={(e) =>
                setStandardPrice(
                  e.target.value
                )
              }
              placeholder="Enter standard price"
              min="0"
              required
            />

            <label>
              Venue
            </label>

            <select
              value={venueId}
              onChange={(e) =>
                setVenueId(
                  e.target.value
                )
              }
              required
            >

              <option value="">
                Select a venue
              </option>

              {venues.map(
                (venue) => (

                  <option
                    key={venue.id}
                    value={venue.id}
                  >
                    {venue.name} -{" "}
                    {venue.location}
                  </option>

                )
              )}

            </select>

            <button
              type="submit"
              className="confirm-button"
            >
              Create Event
            </button>

          </form>

          {message && (
            <p className="events-message">
              {message}
            </p>
          )}

        </div>

        {/* MY EVENTS */}

        <div>

          <h2>
            My Events
          </h2>

          {loadingEvents ? (

            <p className="events-message">
              Loading your events...
            </p>

          ) : events.length === 0 ? (

            <p className="events-message">
              You haven't created any
              events yet.
            </p>

          ) : (

            <div className="event-grid">

              {events.map(
                (event) => (

                  <EventCard
                    key={event.id}
                    event={event}
                  />

                )
              )}

            </div>
          )}

        </div>

      </div>

    </div>
  );
}

export default Organiser;