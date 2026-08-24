import { useEffect, useState } from "react";

function WaitlistOffer() {

    const [message, setMessage] = useState(
        "Loading your seat offer..."
    );

    const [offer, setOffer] = useState(null);

    const [loading, setLoading] = useState(true);

    // =====================================================
    // GET WAITLIST ID FROM URL
    // =====================================================

    const getWaitlistId = () => {

        const path =
            window.location.pathname;

        const parts =
            path.split("/");

        return parts[parts.length - 1];
    };

    // =====================================================
    // LOAD OFFER
    // =====================================================

    useEffect(() => {

        const waitlistId =
            getWaitlistId();

        if (!waitlistId) {

            setMessage(
                "Invalid waitlist offer."
            );

            setLoading(false);

            return;
        }

        loadOffer(waitlistId);

    }, []);

    // =====================================================
    // LOAD OFFER DETAILS
    // =====================================================

    const loadOffer = async (
        waitlistId
    ) => {

        try {

            const token =
                localStorage.getItem("token");

            const response =
                await fetch(
                    `/api/waitlist/event-offer/${waitlistId}`,
                    {
                        headers: token
                            ? {
                                  Authorization:
                                      `Bearer ${token}`,
                              }
                            : {},
                    }
                );

            if (!response.ok) {

                const errorText =
                    await response.text();

                setMessage(
                    errorText ||
                    "Unable to load seat offer."
                );

                setLoading(false);

                return;
            }

            const data =
                await response.json();

            setOffer(data);

            setMessage("");

            setLoading(false);

        } catch (error) {

            console.error(error);

            setMessage(
                "Unable to connect to server."
            );

            setLoading(false);
        }
    };

    // =====================================================
    // ACCEPT OFFER
    // =====================================================

    const acceptOffer = async () => {

        if (!offer) {
            return;
        }

        try {

            const userId =
                localStorage.getItem("userId");

            const token =
                localStorage.getItem("token");

            if (!userId) {

                setMessage(
                    "Please login before accepting the offer."
                );

                return;
            }

            const response =
                await fetch(
                    `/api/waitlist/${offer.id}/accept?userId=${userId}`,
                    {
                        method: "POST",

                        headers: {
                            Authorization:
                                `Bearer ${token}`,
                        },
                    }
                );

            const responseText =
                await response.text();

            if (!response.ok) {

                setMessage(
                    responseText ||
                    "Unable to accept the offer."
                );

                return;
            }

            setMessage(
                "Seat booked successfully!"
            );

            setOffer(null);

            // =================================================
            // SHOW BOOKING RESPONSE
            // =================================================

            try {

                const booking =
                    JSON.parse(responseText);

                setMessage(
                    `Booking confirmed successfully! Booking ID: ${
                        booking.bookingId ||
                        booking.id ||
                        "-"
                    }`
                );

            } catch {

                // Response may not be JSON

            }

        } catch (error) {

            console.error(error);

            setMessage(
                "Unable to connect to server."
            );
        }
    };

    // =====================================================
    // LOADING
    // =====================================================

    if (loading) {

        return (
            <div
                style={{
                    minHeight: "100vh",
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    background: "#f3f4f6",
                }}
            >

                <div
                    style={{
                        background: "white",
                        padding: "40px",
                        borderRadius: "12px",
                        textAlign: "center",
                        boxShadow:
                            "0 4px 20px rgba(0,0,0,0.1)",
                    }}
                >

                    <h2>
                        Loading Seat Offer...
                    </h2>

                </div>

            </div>
        );
    }

    // =====================================================
    // PAGE
    // =====================================================

    return (

        <div
            style={{
                minHeight: "100vh",
                background: "#f3f4f6",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                padding: "20px",
            }}
        >

            <div
                style={{
                    background: "white",
                    width: "100%",
                    maxWidth: "550px",
                    padding: "40px",
                    borderRadius: "15px",
                    textAlign: "center",
                    boxShadow:
                        "0 5px 25px rgba(0,0,0,0.12)",
                }}
            >

                <h1
                    style={{
                        marginBottom: "10px",
                    }}
                >
                    🎟️ Ticket Booking
                </h1>

                <h2
                    style={{
                        marginBottom: "25px",
                    }}
                >
                    Seat Available!
                </h2>

                {offer ? (

                    <>

                        <p>
                            A seat has become
                            available for you.
                        </p>

                        <div
                            style={{
                                marginTop: "25px",
                                marginBottom: "25px",
                                padding: "20px",
                                borderRadius: "10px",
                                background: "#f8fafc",
                                textAlign: "left",
                            }}
                        >

                            <p>
                                <strong>
                                    Event:
                                </strong>{" "}
                                {offer.eventTitle ||
                                    offer.event?.title ||
                                    "-"}
                            </p>

                            <p>
                                <strong>
                                    Seat:
                                </strong>{" "}
                                {offer.seatNumber ||
                                    offer.offeredSeat?.seat?.seatNumber ||
                                    "-"}
                            </p>

                            <p>
                                <strong>
                                    Category:
                                </strong>{" "}
                                {offer.category ||
                                    offer.offeredSeat?.seat?.category ||
                                    "-"}
                            </p>

                            <p>
                                <strong>
                                    Status:
                                </strong>{" "}
                                {offer.status ||
                                    "OFFERED"}
                            </p>

                            <p>
                                <strong>
                                    Offer Expires:
                                </strong>{" "}
                                {offer.offerExpiresAt
                                    ? new Date(
                                          offer.offerExpiresAt
                                      ).toLocaleString()
                                    : "-"}
                            </p>

                        </div>

                        <button
                            onClick={acceptOffer}
                            style={{
                                width: "100%",
                                padding: "14px",
                                border: "none",
                                borderRadius: "8px",
                                background:
                                    "#2563eb",
                                color: "white",
                                fontSize: "16px",
                                fontWeight: "bold",
                                cursor: "pointer",
                            }}
                        >
                            Accept Seat & Book
                        </button>

                    </>

                ) : (

                    <div>

                        <p>
                            {message}
                        </p>

                    </div>

                )}

                {message &&
                    offer && (

                        <p
                            style={{
                                marginTop: "20px",
                                fontWeight: "bold",
                            }}
                        >
                            {message}
                        </p>

                    )}

                <button
                    onClick={() =>
                        window.location.href = "/"
                    }
                    style={{
                        marginTop: "20px",
                        padding: "10px 20px",
                        border: "none",
                        borderRadius: "7px",
                        background: "#6b7280",
                        color: "white",
                        cursor: "pointer",
                    }}
                >
                    Back to Home
                </button>

            </div>

        </div>
    );
}

export default WaitlistOffer;