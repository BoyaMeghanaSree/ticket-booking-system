package com.ticketbooking.controller;

import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.SeatCategory;
import com.ticketbooking.entity.Venue;
import com.ticketbooking.repository.SeatRepository;
import com.ticketbooking.repository.VenueRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class SeatController {

    private final SeatRepository seatRepository;
    private final VenueRepository venueRepository;

    public SeatController(
            SeatRepository seatRepository,
            VenueRepository venueRepository) {

        this.seatRepository = seatRepository;
        this.venueRepository = venueRepository;
    }

    @PostMapping("/{venueId}/seats")
    public ResponseEntity<?> createSeat(
            @PathVariable Long venueId,
            @RequestBody SeatRequest request) {

        Venue venue = venueRepository
                .findById(venueId)
                .orElse(null);

        if (venue == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Venue not found");
        }

        Seat seat = new Seat(
                request.seatNumber,
                request.category,
                venue);

        Seat savedSeat = seatRepository.save(seat);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedSeat);
    }

    @GetMapping("/{venueId}/seats")
    public ResponseEntity<?> getSeats(
            @PathVariable Long venueId) {

        if (!venueRepository.existsById(venueId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Venue not found");
        }

        List<Seat> seats = seatRepository.findByVenueId(venueId);

        return ResponseEntity.ok(seats);
    }

    @PutMapping("/{venueId}/seats/{seatId}")
    public ResponseEntity<?> updateSeat(
            @PathVariable Long venueId,
            @PathVariable Long seatId,
            @RequestBody SeatRequest request) {

        Venue venue = venueRepository
                .findById(venueId)
                .orElse(null);

        if (venue == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Venue not found");
        }

        Seat seat = seatRepository
                .findById(seatId)
                .orElse(null);

        if (seat == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Seat not found");
        }

        if (!seat.getVenue().getId().equals(venueId)) {
            return ResponseEntity
                    .badRequest()
                    .body("Seat does not belong to this venue");
        }

        seat.setSeatNumber(request.seatNumber);
        seat.setCategory(request.category);

        Seat updatedSeat = seatRepository.save(seat);

        return ResponseEntity.ok(updatedSeat);
    }

    public static class SeatRequest {

        public String seatNumber;
        public SeatCategory category;
    }
}