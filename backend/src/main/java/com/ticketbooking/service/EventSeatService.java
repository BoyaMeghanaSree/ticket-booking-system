package com.ticketbooking.service;

import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.EventSeatStatus;
import com.ticketbooking.entity.User;

import com.ticketbooking.repository.EventSeatRepository;
import com.ticketbooking.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class EventSeatService {

        private final EventSeatRepository eventSeatRepository;
        private final UserRepository userRepository;

        public EventSeatService(
                        EventSeatRepository eventSeatRepository,
                        UserRepository userRepository) {

                this.eventSeatRepository = eventSeatRepository;
                this.userRepository = userRepository;
        }

        // =====================================================
        // HOLD SEAT
        // =====================================================

        @Transactional
        public EventSeat holdSeat(
                        Long eventSeatId,
                        Long userId) {

                // =================================================
                // GET SEAT WITH PESSIMISTIC WRITE LOCK
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

                LocalDateTime now = LocalDateTime.now(
                                ZoneId.of("Asia/Kolkata"));

                // =================================================
                // CHECK EXISTING HOLD
                // =================================================

                if (eventSeat.getStatus() == EventSeatStatus.HELD) {

                        // -------------------------------------------------
                        // HOLD IS STILL ACTIVE
                        // -------------------------------------------------

                        if (eventSeat.getHoldExpiresAt() != null
                                        && eventSeat
                                                        .getHoldExpiresAt()
                                                        .isAfter(now)) {

                                // Same user cannot hold again

                                if (eventSeat.getHeldBy() != null
                                                && eventSeat
                                                                .getHeldBy()
                                                                .getId()
                                                                .equals(userId)) {

                                        throw new RuntimeException(
                                                        "You already hold this seat");
                                }

                                // Another user cannot take it

                                throw new RuntimeException(
                                                "Seat is currently held by another user");
                        }

                        // -------------------------------------------------
                        // HOLD HAS EXPIRED
                        // -------------------------------------------------

                        eventSeat.setStatus(
                                        EventSeatStatus.AVAILABLE);

                        eventSeat.setHoldExpiresAt(null);

                        eventSeat.setHeldBy(null);

                        eventSeatRepository.save(eventSeat);
                }

                // =================================================
                // CHECK BOOKED
                // =================================================

                if (eventSeat.getStatus() == EventSeatStatus.BOOKED) {

                        throw new RuntimeException(
                                        "Seat is already booked");
                }

                // =================================================
                // CREATE NEW HOLD
                // =================================================

                eventSeat.setStatus(
                                EventSeatStatus.HELD);

                eventSeat.setHeldBy(user);

                // Hold for 10 minutes

                eventSeat.setHoldExpiresAt(
                                now.plusMinutes(10));

                EventSeat savedSeat = eventSeatRepository.save(eventSeat);

                System.out.println(
                                "Seat "
                                                + savedSeat.getId()
                                                + " held by user "
                                                + userId
                                                + " until "
                                                + savedSeat.getHoldExpiresAt());

                return savedSeat;
        }
}