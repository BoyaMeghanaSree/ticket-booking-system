package com.ticketbooking.controller;

import com.ticketbooking.entity.Venue;
import com.ticketbooking.repository.VenueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueRepository venueRepository;

    public VenueController(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @PostMapping
    public ResponseEntity<Venue> createVenue(
            @RequestBody Venue venue) {

        Venue savedVenue = venueRepository.save(venue);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedVenue);
    }

    @GetMapping
    public ResponseEntity<List<Venue>> getAllVenues() {

        return ResponseEntity.ok(
                venueRepository.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVenue(
            @PathVariable Long id) {

        if (!venueRepository.existsById(id)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Venue not found");
        }

        venueRepository.deleteById(id);

        return ResponseEntity.ok(
                "Venue deleted successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVenueById(
            @PathVariable Long id) {

        return venueRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(
                        ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(null));
    }
}