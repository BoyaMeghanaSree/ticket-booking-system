package com.ticketbooking.controller;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.SeatCategory;
import com.ticketbooking.entity.Waitlist;
import com.ticketbooking.service.WaitlistService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(
            WaitlistService waitlistService) {

        this.waitlistService = waitlistService;
    }

    // =====================================================
    // GET EVENT WAITLIST
    // =====================================================

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventWaitlist(
            @PathVariable Long eventId) {

        try {

            List<Waitlist> waitlist = waitlistService.getEventWaitlist(
                    eventId);

            return ResponseEntity.ok(waitlist);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // JOIN WAITLIST
    // =====================================================

    @PostMapping("/join")
    public ResponseEntity<?> joinWaitlist(
            @RequestParam Long eventId,
            @RequestParam Long userId,
            @RequestParam SeatCategory category) {

        try {

            Waitlist waitlist = waitlistService.joinWaitlist(
                    eventId,
                    userId,
                    category);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(waitlist);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // GET WAITLIST OFFER DETAILS
    // =====================================================

    @GetMapping("/event-offer/{waitlistId}")
    public ResponseEntity<?> getWaitlistOffer(
            @PathVariable Long waitlistId) {

        try {

            Waitlist waitlist = waitlistService
                    .getWaitlistById(waitlistId);

            if (waitlist == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Waitlist offer not found");
            }

            // =================================================
            // CHECK OFFER STATUS
            // =================================================

            if (waitlist.getStatus() == null
                    || !waitlist.getStatus()
                            .name()
                            .equals("OFFERED")) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "No active offer exists for this waitlist entry");
            }

            // =================================================
            // CHECK OFFERED SEAT
            // =================================================

            if (waitlist.getOfferedSeat() == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "No seat is attached to this offer");
            }

            // =================================================
            // CREATE SIMPLE RESPONSE
            // =================================================

            Map<String, Object> response = new HashMap<>();

            response.put(
                    "id",
                    waitlist.getId());

            response.put(
                    "status",
                    waitlist.getStatus());

            response.put(
                    "category",
                    waitlist.getCategory());

            response.put(
                    "offerExpiresAt",
                    waitlist.getOfferExpiresAt());

            response.put(
                    "eventTitle",
                    waitlist.getEvent()
                            .getTitle());

            response.put(
                    "seatNumber",
                    waitlist
                            .getOfferedSeat()
                            .getSeat()
                            .getSeatNumber());

            response.put(
                    "eventId",
                    waitlist.getEvent()
                            .getId());

            response.put(
                    "eventSeatId",
                    waitlist
                            .getOfferedSeat()
                            .getId());

            return ResponseEntity.ok(
                    response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =====================================================
    // ACCEPT WAITLIST OFFER
    // =====================================================

    @PostMapping("/{waitlistId}/accept")
    public ResponseEntity<?> acceptOffer(
            @PathVariable Long waitlistId,
            @RequestParam Long userId) {

        try {

            Booking booking = waitlistService.acceptOffer(
                    waitlistId,
                    userId);

            return ResponseEntity.ok(
                    booking);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}