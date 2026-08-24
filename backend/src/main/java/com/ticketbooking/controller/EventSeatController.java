package com.ticketbooking.controller;

import com.ticketbooking.service.EventSeatService;
import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.EventSeatStatus;
import com.ticketbooking.entity.Seat;

import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.EventSeatRepository;
import com.ticketbooking.repository.SeatRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventSeatController {
        private final EventSeatService eventSeatService;
        private final EventRepository eventRepository;
        private final EventSeatRepository eventSeatRepository;
        private final SeatRepository seatRepository;

        public EventSeatController(
                        EventRepository eventRepository,
                        EventSeatRepository eventSeatRepository,
                        SeatRepository seatRepository,
                        EventSeatService eventSeatService) {

                this.eventRepository = eventRepository;
                this.eventSeatRepository = eventSeatRepository;
                this.seatRepository = seatRepository;
                this.eventSeatService = eventSeatService;
        }

        @PostMapping("/{eventId}/seats/initialize")
        public ResponseEntity<?> initializeSeats(
                        @PathVariable Long eventId) {

                Event event = eventRepository
                                .findById(eventId)
                                .orElse(null);

                if (event == null) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body("Event not found");
                }

                List<Seat> venueSeats = seatRepository.findByVenueId(
                                event.getVenue().getId());

                List<EventSeat> existingSeats = eventSeatRepository.findByEventId(eventId);

                if (!existingSeats.isEmpty()) {
                        return ResponseEntity
                                        .status(HttpStatus.BAD_REQUEST)
                                        .body("Event seats are already initialized");
                }

                List<EventSeat> eventSeats = new ArrayList<>();

                for (Seat seat : venueSeats) {

                        EventSeat eventSeat = new EventSeat(
                                        event,
                                        seat,
                                        EventSeatStatus.AVAILABLE);

                        eventSeats.add(eventSeat);
                }

                List<EventSeat> savedSeats = eventSeatRepository.saveAll(eventSeats);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(savedSeats.size()
                                                + " event seats created");
        }

        @GetMapping("/{eventId}/seats")
        public ResponseEntity<?> getEventSeats(
                        @PathVariable Long eventId) {

                if (!eventRepository.existsById(eventId)) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body("Event not found");
                }

                List<EventSeat> seats = eventSeatRepository.findByEventId(eventId);

                return ResponseEntity.ok(seats);
        }

        @PostMapping("/{eventId}/seats/{eventSeatId}/hold")
        public ResponseEntity<?> holdSeat(
                        @PathVariable Long eventId,
                        @PathVariable Long eventSeatId,
                        @RequestParam Long userId) {

                try {

                        EventSeat eventSeat = eventSeatRepository
                                        .findById(eventSeatId)
                                        .orElse(null);

                        if (eventSeat == null) {
                                return ResponseEntity
                                                .status(HttpStatus.NOT_FOUND)
                                                .body("Event seat not found");
                        }

                        if (!eventSeat.getEvent()
                                        .getId()
                                        .equals(eventId)) {

                                return ResponseEntity
                                                .badRequest()
                                                .body("Seat does not belong to this event");
                        }

                        EventSeat heldSeat = eventSeatService.holdSeat(
                                        eventSeatId,
                                        userId);

                        return ResponseEntity.ok(heldSeat);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }
}