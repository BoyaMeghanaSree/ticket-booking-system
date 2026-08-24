import { useEffect, useState } from "react";
import "./Events.css";

function Admin({ onLogout }) {
  const [venues, setVenues] = useState([]);

  const [showCreateVenue, setShowCreateVenue] =
    useState(false);

  const [selectedVenue, setSelectedVenue] =
    useState(null);

  const [venueSeats, setVenueSeats] = useState([]);

  const [venueName, setVenueName] = useState("");
  const [venueLocation, setVenueLocation] =
    useState("");

  const [seatNumber, setSeatNumber] =
    useState("");

  const [seatCategory, setSeatCategory] =
    useState("PREMIUM");

  const [message, setMessage] = useState("");

  useEffect(() => {
    loadVenues();
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
  // CREATE VENUE
  // =========================

  const createVenue = async (e) => {
    e.preventDefault();

    try {
      const token = localStorage.getItem("token");

      const response = await fetch("/api/venues", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },

        body: JSON.stringify({
          name: venueName,
          location: venueLocation,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        setMessage(
          typeof data === "string"
            ? data
            : "Unable to create venue"
        );
        return;
      }

      setMessage("Venue created successfully!");

      setVenueName("");
      setVenueLocation("");
      setShowCreateVenue(false);

      await loadVenues();

    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // LOAD VENUE SEATS
  // =========================

  const loadSeats = async (venue) => {
    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `/api/venues/${venue.id}/seats`,
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

      setSelectedVenue(venue);
      setVenueSeats(data);
      setMessage("");

    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // ADD SEAT
  // =========================

  const addSeat = async (e) => {
    e.preventDefault();

    if (!selectedVenue) {
      setMessage("Please select a venue");
      return;
    }

    try {
      const token = localStorage.getItem("token");

      const response = await fetch(
        `/api/venues/${selectedVenue.id}/seats`,
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },

          body: JSON.stringify({
            seatNumber: seatNumber,
            category: seatCategory,
          }),
        }
      );

      const data = await response.json();

      if (!response.ok) {
        setMessage(
          typeof data === "string"
            ? data
            : "Unable to add seat"
        );
        return;
      }

      setMessage(
        `Seat ${seatNumber} added successfully!`
      );

      setSeatNumber("");

      await loadSeats(selectedVenue);

      await loadVenues();

    } catch (error) {
      console.error(error);
      setMessage("Unable to connect to server");
    }
  };

  // =========================
  // CLOSE SEAT MANAGEMENT
  // =========================

  const closeSeatManagement = () => {
    setSelectedVenue(null);
    setVenueSeats([]);
    setSeatNumber("");
    setMessage("");
  };

  return (
    <div className="events-container">

      {/* =========================
          HEADER
      ========================= */}

      <div className="events-header">

        <div>
          <h1>Ticket Booking</h1>

          <p>Admin Dashboard</p>
        </div>

        <button
          className="logout-button"
          onClick={onLogout}
        >
          Logout
        </button>

      </div>

      <div className="events-content">

        {/* =========================
            MESSAGE
        ========================= */}

        {message && (
          <p className="events-message">
            {message}
          </p>
        )}

        {/* =========================
            SEAT MANAGEMENT
        ========================= */}

        {selectedVenue ? (

          <div>

            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "20px",
              }}
            >

              <div>

                <h2>
                  Manage Seats
                </h2>

                <p>
                  {selectedVenue.name} -{" "}
                  {selectedVenue.location}
                </p>

              </div>

              <button
                className="secondary-button"
                onClick={closeSeatManagement}
              >
                Back to Venues
              </button>

            </div>

            {/* =========================
                ADD SEAT
            ========================= */}

            <div className="confirmation-box">

              <h2>
                Add Seat
              </h2>

              <form onSubmit={addSeat}>

                <label>
                  Seat Number
                </label>

                <input
                  type="text"
                  placeholder="Example: A1"
                  value={seatNumber}
                  onChange={(e) =>
                    setSeatNumber(
                      e.target.value.toUpperCase()
                    )
                  }
                  required
                />

                <label>
                  Seat Category
                </label>

                <select
                  value={seatCategory}
                  onChange={(e) =>
                    setSeatCategory(
                      e.target.value
                    )
                  }
                >

                  <option value="PREMIUM">
                    Premium
                  </option>

                  <option value="STANDARD">
                    Standard
                  </option>

                </select>

                <button
                  type="submit"
                  className="confirm-button"
                >
                  Add Seat
                </button>

              </form>

            </div>

            {/* =========================
                SEAT LIST
            ========================= */}

            <h2>
              Seats ({venueSeats.length})
            </h2>

            {venueSeats.length === 0 ? (

              <p>
                No seats added to this venue yet.
              </p>

            ) : (

              <div className="event-grid">

                {venueSeats.map((seat) => (

                  <div
                    className="event-card"
                    key={seat.id}
                  >

                    <h3>
                      {seat.seatNumber}
                    </h3>

                    <p>
                      Category:{" "}
                      <strong>
                        {seat.category}
                      </strong>
                    </p>

                    <p>
                      Seat ID: {seat.id}
                    </p>

                  </div>

                ))}

              </div>

            )}

          </div>

        ) : (

          /* =========================
             VENUE MANAGEMENT
             ========================= */

          <div>

            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: "20px",
              }}
            >

              <h2>
                Manage Venues
              </h2>

              <button
                className="book-button"
                onClick={() => {
                  setShowCreateVenue(
                    !showCreateVenue
                  );

                  setMessage("");
                }}
              >
                {showCreateVenue
                  ? "Cancel"
                  : "+ Add Venue"}
              </button>

            </div>

            {/* =========================
                CREATE VENUE
            ========================= */}

            {showCreateVenue && (

              <div className="confirmation-box">

                <h2>
                  Create New Venue
                </h2>

                <form
                  onSubmit={createVenue}
                >

                  <label>
                    Venue Name
                  </label>

                  <input
                    type="text"
                    placeholder="Enter venue name"
                    value={venueName}
                    onChange={(e) =>
                      setVenueName(
                        e.target.value
                      )
                    }
                    required
                  />

                  <label>
                    Location
                  </label>

                  <input
                    type="text"
                    placeholder="Enter venue location"
                    value={venueLocation}
                    onChange={(e) =>
                      setVenueLocation(
                        e.target.value
                      )
                    }
                    required
                  />

                  <button
                    type="submit"
                    className="confirm-button"
                  >
                    Create Venue
                  </button>

                </form>

              </div>

            )}

            {/* =========================
                VENUE LIST
            ========================= */}

            {venues.length === 0 ? (

              <p>
                No venues available.
              </p>

            ) : (

              <div className="event-grid">

                {venues.map((venue) => (

                  <div
                    className="event-card"
                    key={venue.id}
                  >

                    <h3>
                      {venue.name}
                    </h3>

                    <p>
                      <strong>
                        Location:
                      </strong>{" "}
                      {venue.location}
                    </p>

                    <p>
                      <strong>
                        Venue ID:
                      </strong>{" "}
                      {venue.id}
                    </p>

                    <p>
                      <strong>
                        Total Seats:
                      </strong>{" "}
                      {venue.seats
                        ? venue.seats.length
                        : 0}
                    </p>

                    <button
                      className="book-button"
                      onClick={() =>
                        loadSeats(venue)
                      }
                    >
                      Manage Seats
                    </button>

                  </div>

                ))}

              </div>

            )}

          </div>

        )}

      </div>

    </div>
  );
}

export default Admin;