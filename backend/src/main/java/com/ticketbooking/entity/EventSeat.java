package com.ticketbooking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "event_id", "seat_id" })
})
public class EventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventSeatStatus status;

    private LocalDateTime holdExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by")
    @JsonIgnore
    private User heldBy;

    public EventSeat() {
    }

    public EventSeat(
            Event event,
            Seat seat,
            EventSeatStatus status) {

        this.event = event;
        this.seat = seat;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public EventSeatStatus getStatus() {
        return status;
    }

    public void setStatus(EventSeatStatus status) {
        this.status = status;
    }

    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public User getHeldBy() {
        return heldBy;
    }

    public void setHeldBy(User heldBy) {
        this.heldBy = heldBy;
    }
}