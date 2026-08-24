package com.ticketbooking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "venue_id", "seat_number" })
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatCategory category;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    public Seat() {
    }

    public Seat(
            String seatNumber,
            SeatCategory category,
            Venue venue) {

        this.seatNumber = seatNumber;
        this.category = category;
        this.venue = venue;
    }

    public Long getId() {
        return id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatCategory getCategory() {
        return category;
    }

    public void setCategory(SeatCategory category) {
        this.category = category;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }
}