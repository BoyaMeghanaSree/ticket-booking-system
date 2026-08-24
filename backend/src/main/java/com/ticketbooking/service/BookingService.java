package com.ticketbooking.service;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.BookingStatus;
import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.EventSeatStatus;
import com.ticketbooking.entity.SeatCategory;
import com.ticketbooking.entity.User;

import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventSeatRepository;
import com.ticketbooking.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class BookingService {

        private final BookingRepository bookingRepository;
        private final EventSeatRepository eventSeatRepository;
        private final UserRepository userRepository;
        private final WaitlistService waitlistService;
        private final EmailService emailService;

        public BookingService(
                        BookingRepository bookingRepository,
                        EventSeatRepository eventSeatRepository,
                        UserRepository userRepository,
                        WaitlistService waitlistService,
                        EmailService emailService) {

                this.bookingRepository = bookingRepository;
                this.eventSeatRepository = eventSeatRepository;
                this.userRepository = userRepository;
                this.waitlistService = waitlistService;
                this.emailService = emailService;
        }

        // =====================================================
        // CONFIRM BOOKING
        // =====================================================

        @Transactional
        public Booking confirmBooking(
                        Long eventSeatId,
                        Long userId) {

                // =================================================
                // GET SEAT WITH LOCK
                // =================================================

                EventSeat eventSeat = eventSeatRepository
                                .findByIdWithLock(eventSeatId)
                                .orElse(null);

                if (eventSeat == null) {
                        throw new RuntimeException(
                                        "Event seat not found");
                }

                // =================================================
                // GET USER
                // =================================================

                User user = userRepository
                                .findById(userId)
                                .orElse(null);

                if (user == null) {
                        throw new RuntimeException(
                                        "User not found");
                }

                // =================================================
                // CHECK SEAT STATUS
                // =================================================

                if (eventSeat.getStatus() != EventSeatStatus.HELD) {

                        throw new RuntimeException(
                                        "Seat is not currently held");
                }

                // =================================================
                // CHECK WHO HOLDS THE SEAT
                // =================================================

                if (eventSeat.getHeldBy() == null
                                || !eventSeat
                                                .getHeldBy()
                                                .getId()
                                                .equals(userId)) {

                        throw new RuntimeException(
                                        "Seat is held by another user");
                }

                // =================================================
                // CHECK HOLD EXPIRY
                // =================================================

                LocalDateTime now = LocalDateTime.now(
                                ZoneId.of("Asia/Kolkata"));

                if (eventSeat.getHoldExpiresAt() == null
                                || !eventSeat
                                                .getHoldExpiresAt()
                                                .isAfter(now)) {

                        eventSeat.setStatus(
                                        EventSeatStatus.AVAILABLE);

                        eventSeat.setHoldExpiresAt(null);
                        eventSeat.setHeldBy(null);

                        eventSeatRepository.save(eventSeat);

                        throw new RuntimeException(
                                        "Seat hold has expired");
                }

                // =================================================
                // CALCULATE PRICE
                // =================================================

                Double price;

                if (eventSeat.getSeat()
                                .getCategory() == SeatCategory.PREMIUM) {

                        price = eventSeat
                                        .getEvent()
                                        .getPremiumPrice();

                } else {

                        price = eventSeat
                                        .getEvent()
                                        .getStandardPrice();
                }

                // =================================================
                // BOOK SEAT
                // =================================================

                eventSeat.setStatus(
                                EventSeatStatus.BOOKED);

                eventSeat.setHoldExpiresAt(null);
                eventSeat.setHeldBy(null);

                eventSeatRepository.save(eventSeat);

                // =================================================
                // CREATE BOOKING
                // =================================================

                Booking booking = new Booking(
                                eventSeat.getEvent(),
                                eventSeat,
                                user,
                                BookingStatus.CONFIRMED,
                                now,
                                price);

                Booking savedBooking = bookingRepository.save(booking);

                // =================================================
                // SEND BOOKING CONFIRMATION EMAIL
                // =================================================

                try {

                        emailService.sendBookingConfirmation(
                                        savedBooking);

                        System.out.println(
                                        "Booking confirmation email sent to "
                                                        + user.getEmail());

                } catch (Exception e) {

                        System.out.println(
                                        "Booking successful but email could not be sent");

                        e.printStackTrace();
                }

                // =================================================
                // RETURN BOOKING
                // =================================================

                return savedBooking;
        }

        // =====================================================
        // CANCEL BOOKING
        // =====================================================

        @Transactional
        public Booking cancelBooking(
                        Long bookingId,
                        Long userId) {

                // =================================================
                // GET BOOKING
                // =================================================

                Booking booking = bookingRepository
                                .findById(bookingId)
                                .orElse(null);

                if (booking == null) {
                        throw new RuntimeException(
                                        "Booking not found");
                }

                // =================================================
                // CHECK BOOKING OWNER
                // =================================================

                if (!booking.getUser()
                                .getId()
                                .equals(userId)) {

                        throw new RuntimeException(
                                        "You can cancel only your own booking");
                }

                // =================================================
                // CHECK ALREADY CANCELLED
                // =================================================

                if (booking.getStatus() == BookingStatus.CANCELLED) {

                        throw new RuntimeException(
                                        "Booking is already cancelled");
                }

                // =================================================
                // GET EVENT SEAT
                // =================================================

                EventSeat eventSeat = booking.getEventSeat();

                // =================================================
                // CANCEL BOOKING
                // =================================================

                booking.setStatus(
                                BookingStatus.CANCELLED);

                bookingRepository.save(booking);

                // =================================================
                // RELEASE SEAT
                // =================================================

                eventSeat.setStatus(
                                EventSeatStatus.AVAILABLE);

                eventSeat.setHoldExpiresAt(null);
                eventSeat.setHeldBy(null);

                eventSeatRepository.save(eventSeat);

                // =================================================
                // OFFER TO NEXT WAITLIST CUSTOMER
                // =================================================

                waitlistService.offerSeatToNextCustomer(
                                eventSeat);

                return booking;
        }
}