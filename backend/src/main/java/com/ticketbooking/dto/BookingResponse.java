package com.ticketbooking.dto;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.User;

import java.time.LocalDateTime;

public class BookingResponse {

    private Long bookingId;

    private String customerName;
    private String customerEmail;

    private String eventTitle;

    private Long eventSeatId;
    private String seatNumber;
    private String category;

    private Double price;

    private String status;
    private LocalDateTime bookedAt;

    public BookingResponse(Booking booking) {

        this.bookingId = booking.getId();

        User user = booking.getUser();

        if (user != null) {
            this.customerName = user.getName();
            this.customerEmail = user.getEmail();
        }

        if (booking.getEvent() != null) {
            this.eventTitle = booking.getEvent().getTitle();
        }

        EventSeat eventSeat = booking.getEventSeat();

        if (eventSeat != null) {

            this.eventSeatId = eventSeat.getId();

            Seat seat = eventSeat.getSeat();

            if (seat != null) {

                this.seatNumber = seat.getSeatNumber();

                if (seat.getCategory() != null) {
                    this.category = seat.getCategory().name();
                }
            }
        }

        this.price = booking.getPrice();

        if (booking.getStatus() != null) {
            this.status = booking.getStatus().name();
        }

        this.bookedAt = booking.getBookedAt();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public Long getEventSeatId() {
        return eventSeatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getBookedAt() {
        return bookedAt;
    }
}