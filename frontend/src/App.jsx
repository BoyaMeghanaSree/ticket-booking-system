import { useState } from "react";
import "./App.css";

import Events from "./Events";
import Organiser from "./Organiser";
import Admin from "./Admin";
import WaitlistOffer from "./WaitlistOffer";

function App() {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [role, setRole] = useState("CUSTOMER");

  const [message, setMessage] = useState("");
  const [showRegister, setShowRegister] = useState(false);

  const [loggedIn, setLoggedIn] = useState(
    !!localStorage.getItem("token")
  );

  // =====================================================
  // WAITLIST OFFER PAGE
  // =====================================================

  const currentPath = window.location.pathname;

  if (currentPath.startsWith("/waitlist-offer/")) {
    return <WaitlistOffer />;
  }

  // =====================================================
  // LOGIN
  // =====================================================

  const handleLogin = async (e) => {

    e.preventDefault();

    try {

      const response = await fetch(
        "/api/auth/login",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            email,
            password,
          }),
        }
      );

      const data = await response.json();

      if (response.ok) {

        localStorage.setItem(
          "token",
          data.token
        );

        localStorage.setItem(
          "userId",
          data.userId
        );

        localStorage.setItem(
          "name",
          data.name
        );

        localStorage.setItem(
          "email",
          data.email
        );

        localStorage.setItem(
          "role",
          data.role
        );

        setMessage("");

        setLoggedIn(true);

      } else {

        setMessage(
          typeof data === "string"
            ? data
            : "Invalid email or password"
        );
      }

    } catch (error) {

      console.error(error);

      setMessage(
        "Unable to connect to server"
      );
    }
  };

  // =====================================================
  // REGISTER
  // =====================================================

  const handleRegister = async (e) => {

    e.preventDefault();

    try {

      const response = await fetch(
        "/api/auth/register",
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            name,
            email,
            password,
            role,
          }),
        }
      );

      const data = await response.text();

      if (response.ok) {

        setMessage(
          "Registration successful! Please login."
        );

        setName("");
        setEmail("");
        setPassword("");
        setRole("CUSTOMER");

        setShowRegister(false);

      } else {

        setMessage(
          data || "Registration failed"
        );
      }

    } catch (error) {

      console.error(error);

      setMessage(
        "Unable to connect to server"
      );
    }
  };

  // =====================================================
  // LOGOUT
  // =====================================================

  const handleLogout = () => {

    localStorage.clear();

    setLoggedIn(false);

    setEmail("");
    setPassword("");
    setMessage("");
  };

  // =====================================================
  // LOGGED-IN PAGES
  // =====================================================

  if (loggedIn) {

    const currentRole =
      localStorage.getItem("role");

    // =================================================
    // ADMIN
    // =================================================

    if (currentRole === "ADMIN") {

      return (
        <Admin
          onLogout={handleLogout}
        />
      );
    }

    // =================================================
    // ORGANISER
    // =================================================

    if (currentRole === "ORGANISER") {

      return (
        <Organiser
          onLogout={handleLogout}
        />
      );
    }

    // =================================================
    // CUSTOMER
    // =================================================

    return (
      <Events
        onLogout={handleLogout}
      />
    );
  }

  // =====================================================
  // REGISTER PAGE
  // =====================================================

  if (showRegister) {

    return (

      <div className="login-container">

        <div className="login-card">

          <h1>
            Create Account
          </h1>

          <p className="subtitle">
            Register for Ticket Booking
          </p>

          <form
            onSubmit={handleRegister}
          >

            <label>
              Name
            </label>

            <input
              type="text"
              placeholder="Enter your name"
              value={name}
              onChange={(e) =>
                setName(e.target.value)
              }
              required
            />

            <label>
              Email
            </label>

            <input
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) =>
                setEmail(e.target.value)
              }
              required
            />

            <label>
              Password
            </label>

            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) =>
                setPassword(e.target.value)
              }
              required
            />

            <label>
              Role
            </label>

            <select
              value={role}
              onChange={(e) =>
                setRole(e.target.value)
              }
            >

              <option value="CUSTOMER">
                Customer
              </option>

              <option value="ORGANISER">
                Organiser
              </option>

            </select>

            <button type="submit">
              Register
            </button>

          </form>

          {message && (

            <p className="message">
              {message}
            </p>

          )}

          <button
            className="secondary-button"
            onClick={() => {

              setShowRegister(false);

              setMessage("");

            }}
          >
            Back to Login
          </button>

        </div>

      </div>
    );
  }

  // =====================================================
  // LOGIN PAGE
  // =====================================================

  return (

    <div className="login-container">

      <div className="login-card">

        <h1>
          Ticket Booking
        </h1>

        <p className="subtitle">
          Book your tickets easily
        </p>

        <form
          onSubmit={handleLogin}
        >

          <label>
            Email
          </label>

          <input
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) =>
              setEmail(e.target.value)
            }
            required
          />

          <label>
            Password
          </label>

          <input
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) =>
              setPassword(e.target.value)
            }
            required
          />

          <button type="submit">
            Login
          </button>

        </form>

        {message && (

          <p className="message">
            {message}
          </p>

        )}

        <button
          className="secondary-button"
          onClick={() => {

            setShowRegister(true);

            setMessage("");

          }}
        >
          Create New Account
        </button>

      </div>

    </div>
  );
}

export default App;