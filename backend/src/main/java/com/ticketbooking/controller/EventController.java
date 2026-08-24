package com.ticketbooking.controller;

import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.EventSeatStatus;
import com.ticketbooking.entity.Seat;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.Venue;

import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.EventSeatRepository;
import com.ticketbooking.repository.SeatRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.VenueRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final EventSeatRepository eventSeatRepository;
    private final SeatRepository seatRepository;

    public EventController(
            EventRepository eventRepository,
            VenueRepository venueRepository,
            UserRepository userRepository,
            EventSeatRepository eventSeatRepository,
            SeatRepository seatRepository) {

        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.seatRepository = seatRepository;
    }

    // =========================
    // GET ALL EVENTS
    // =========================

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {

        List<Event> events = eventRepository.findAll();

        List<EventResponse> response = events.stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getEventDate(),
                        event.getEventTime(),
                        event.getPremiumPrice(),
                        event.getStandardPrice(),
                        event.getVenue().getName(),
                        event.getVenue().getLocation(),
                        event.getOrganiser().getId()))
                .toList();

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET ORGANISER EVENTS
    // =========================

    @GetMapping("/organiser/{organiserId}")
    public ResponseEntity<?> getOrganiserEvents(
            @PathVariable Long organiserId) {

        User organiser = userRepository
                .findById(organiserId)
                .orElse(null);

        if (organiser == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Organiser not found");
        }

        List<Event> events = eventRepository.findByOrganiserId(organiserId);

        List<EventResponse> response = events.stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getEventDate(),
                        event.getEventTime(),
                        event.getPremiumPrice(),
                        event.getStandardPrice(),
                        event.getVenue().getName(),
                        event.getVenue().getLocation(),
                        event.getOrganiser().getId()))
                .toList();

        return ResponseEntity.ok(response);
    }

    // =========================
    // UPDATE EVENT ORGANISER
    // =========================

    @PutMapping("/{eventId}/organiser/{organiserId}")
    public ResponseEntity<?> updateEventOrganiser(
            @PathVariable Long eventId,
            @PathVariable Long organiserId) {

        Event event = eventRepository
                .findById(eventId)
                .orElse(null);

        if (event == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Event not found");
        }

        User organiser = userRepository
                .findById(organiserId)
                .orElse(null);

        if (organiser == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Organiser not found");
        }

        event.setOrganiser(organiser);

        eventRepository.save(event);

        return ResponseEntity.ok(
                "Event organiser updated successfully");
    }

    // =========================
    // CREATE EVENT
    // =========================

    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody CreateEventRequest request) {

        Venue venue = venueRepository
                .findById(request.venueId)
                .orElse(null);

        if (venue == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Venue not found");
        }

        User organiser = userRepository
                .findById(request.organiserId)
                .orElse(null);

        if (organiser == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Organiser not found");
        }

        Event event = new Event(
                request.title,
                request.description,
                LocalDate.parse(request.eventDate),
                LocalTime.parse(request.eventTime),
                request.premiumPrice,
                request.standardPrice,
                venue,
                organiser);

        Event savedEvent = eventRepository.save(event);

        // =========================
        // CREATE EVENT SEATS
        // =========================

        List<Seat> venueSeats = seatRepository.findByVenueId(
                venue.getId());

        List<EventSeat> eventSeats = new ArrayList<>();

        for (Seat seat : venueSeats) {

            EventSeat eventSeat = new EventSeat(
                    savedEvent,
                    seat,
                    EventSeatStatus.AVAILABLE);

            eventSeats.add(eventSeat);
        }

        if (!eventSeats.isEmpty()) {
            eventSeatRepository.saveAll(
                    eventSeats);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        savedEvent.getId()
                                + " - Event created successfully with "
                                + eventSeats.size()
                                + " seats");
    }

    // =========================
    // CREATE EVENT REQUEST
    // =========================

    public static class CreateEventRequest {

        public String title;
        public String description;
        public String eventDate;
        public String eventTime;

        public Double premiumPrice;
        public Double standardPrice;

        public Long venueId;
        public Long organiserId;
    }

    // =========================
    // EVENT RESPONSE
    // =========================

    public static class EventResponse {

        private final Long id;
        private final String title;
        private final String description;
        private final LocalDate eventDate;
        private final LocalTime eventTime;

        private final Double premiumPrice;
        private final Double standardPrice;

        private final String venueName;
        private final String venueLocation;

        private final Long organiserId;

        public EventResponse(
                Long id,
                String title,
                String description,
                LocalDate eventDate,
                LocalTime eventTime,
                Double premiumPrice,
                Double standardPrice,
                String venueName,
                String venueLocation,
                Long organiserId) {

            this.id = id;
            this.title = title;
            this.description = description;
            this.eventDate = eventDate;
            this.eventTime = eventTime;
            this.premiumPrice = premiumPrice;
            this.standardPrice = standardPrice;
            this.venueName = venueName;
            this.venueLocation = venueLocation;
            this.organiserId = organiserId;
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public LocalDate getEventDate() {
            return eventDate;
        }

        public LocalTime getEventTime() {
            return eventTime;
        }

        public Double getPremiumPrice() {
            return premiumPrice;
        }

        public Double getStandardPrice() {
            return standardPrice;
        }

        public String getVenueName() {
            return venueName;
        }

        public String getVenueLocation() {
            return venueLocation;
        }

        public Long getOrganiserId() {
            return organiserId;
        }
    }
}