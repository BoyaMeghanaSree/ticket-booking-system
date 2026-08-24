package com.ticketbooking.controller;

import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.dto.BookingSummaryResponse;
import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.BookingStatus;
import com.ticketbooking.entity.Event;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.service.BookingService;
import com.ticketbooking.service.QrCodeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

        private final BookingService bookingService;
        private final BookingRepository bookingRepository;
        private final EventRepository eventRepository;
        private final QrCodeService qrCodeService;

        public BookingController(
                        BookingService bookingService,
                        BookingRepository bookingRepository,
                        EventRepository eventRepository,
                        QrCodeService qrCodeService) {

                this.bookingService = bookingService;
                this.bookingRepository = bookingRepository;
                this.eventRepository = eventRepository;
                this.qrCodeService = qrCodeService;
        }

        // =====================================================
        // CONFIRM BOOKING
        // =====================================================

        @PostMapping("/confirm")
        public ResponseEntity<?> confirmBooking(
                        @RequestParam Long eventSeatId,
                        @RequestParam Long userId) {

                try {

                        Booking booking = bookingService.confirmBooking(
                                        eventSeatId,
                                        userId);

                        BookingResponse response = new BookingResponse(booking);

                        // =====================================================
                        // GENERATE QR CODE
                        // =====================================================

                        String bookingReference = "BOOKING-" + booking.getId();

                        byte[] qrBytes = qrCodeService.generateQrCode(
                                        bookingReference);

                        String qrBase64 = Base64.getEncoder()
                                        .encodeToString(qrBytes);

                        String qrDataUrl = "data:image/png;base64,"
                                        + qrBase64;

                        response.setQrCode(qrDataUrl);

                        return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(response);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }

        // =====================================================
        // GET USER BOOKINGS
        // =====================================================

        @GetMapping("/user/{userId}")
        public ResponseEntity<?> getUserBookings(
                        @PathVariable Long userId) {

                try {

                        List<Booking> bookings = bookingRepository.findByUserId(userId);

                        List<BookingResponse> response = bookings.stream()
                                        .map(BookingResponse::new)
                                        .toList();

                        return ResponseEntity.ok(response);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }

        // =====================================================
        // GET ALL BOOKINGS FOR EVENT
        // =====================================================

        @GetMapping("/event/{eventId}")
        public ResponseEntity<?> getEventBookings(
                        @PathVariable Long eventId) {

                try {

                        List<Booking> bookings = bookingRepository.findByEventId(eventId);

                        return ResponseEntity.ok(bookings);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }

        // =====================================================
        // GET ORGANISER EVENT BOOKINGS
        // =====================================================

        @GetMapping("/organiser/{organiserId}/event/{eventId}")
        public ResponseEntity<?> getOrganiserEventBookings(
                        @PathVariable Long organiserId,
                        @PathVariable Long eventId) {

                Event event = eventRepository
                                .findById(eventId)
                                .orElse(null);

                if (event == null) {

                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body("Event not found");
                }

                // =================================================
                // CHECK EVENT OWNER
                // =================================================

                if (event.getOrganiser() == null
                                || !event.getOrganiser()
                                                .getId()
                                                .equals(organiserId)) {

                        return ResponseEntity
                                        .status(HttpStatus.FORBIDDEN)
                                        .body(
                                                        "You can view bookings only for your own events");
                }

                List<Booking> bookings = bookingRepository.findByEventId(eventId);

                List<BookingResponse> response = bookings.stream()
                                .map(BookingResponse::new)
                                .toList();

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // ORGANISER BOOKING SUMMARY + REVENUE
        // =====================================================

        @GetMapping("/organiser/{organiserId}/event/{eventId}/summary")
        public ResponseEntity<?> getEventBookingSummary(
                        @PathVariable Long organiserId,
                        @PathVariable Long eventId) {

                Event event = eventRepository
                                .findById(eventId)
                                .orElse(null);

                if (event == null) {

                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body("Event not found");
                }

                // =================================================
                // CHECK ORGANISER
                // =================================================

                if (event.getOrganiser() == null
                                || !event.getOrganiser()
                                                .getId()
                                                .equals(organiserId)) {

                        return ResponseEntity
                                        .status(HttpStatus.FORBIDDEN)
                                        .body(
                                                        "You can view summary only for your own event");
                }

                // =================================================
                // GET BOOKINGS
                // =================================================

                List<Booking> bookings = bookingRepository.findByEventId(eventId);

                long totalBookings = bookings.size();

                long confirmedBookings = bookings.stream()
                                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                                .count();

                long cancelledBookings = bookings.stream()
                                .filter(booking -> booking.getStatus() == BookingStatus.CANCELLED)
                                .count();

                // =================================================
                // CALCULATE REVENUE
                // =================================================

                double totalRevenue = bookings.stream()
                                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                                .mapToDouble(booking -> booking.getPrice() != null
                                                ? booking.getPrice()
                                                : 0.0)
                                .sum();

                // =================================================
                // CREATE RESPONSE
                // =================================================

                BookingSummaryResponse response = new BookingSummaryResponse(
                                eventId,
                                event.getTitle(),
                                totalBookings,
                                confirmedBookings,
                                cancelledBookings,
                                totalRevenue);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // CANCEL BOOKING
        // =====================================================

        @PostMapping("/{bookingId}/cancel")
        public ResponseEntity<?> cancelBooking(
                        @PathVariable Long bookingId,
                        @RequestParam Long userId) {

                try {

                        Booking booking = bookingService.cancelBooking(
                                        bookingId,
                                        userId);

                        BookingResponse response = new BookingResponse(booking);

                        return ResponseEntity.ok(response);

                } catch (RuntimeException e) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());
                }
        }
}