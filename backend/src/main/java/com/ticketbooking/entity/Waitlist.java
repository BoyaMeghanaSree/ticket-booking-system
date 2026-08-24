package com.ticketbooking.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist")
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_seat_id")
    private EventSeat offeredSeat;

    private LocalDateTime joinedAt;

    private LocalDateTime offerExpiresAt;

    public Waitlist() {
    }

    public Waitlist(
            Event event,
            User user,
            SeatCategory category) {

        this.event = event;
        this.user = user;
        this.category = category;
        this.status = WaitlistStatus.WAITING;
        this.joinedAt = LocalDateTime.now();
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public User getUser() {
        return user;
    }

    public SeatCategory getCategory() {
        return category;
    }

    public WaitlistStatus getStatus() {
        return status;
    }

    public EventSeat getOfferedSeat() {
        return offeredSeat;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getOfferExpiresAt() {
        return offerExpiresAt;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setStatus(
            WaitlistStatus status) {

        this.status = status;
    }

    public void setOfferedSeat(
            EventSeat offeredSeat) {

        this.offeredSeat = offeredSeat;
    }

    public void setOfferExpiresAt(
            LocalDateTime offerExpiresAt) {

        this.offerExpiresAt = offerExpiresAt;
    }
}