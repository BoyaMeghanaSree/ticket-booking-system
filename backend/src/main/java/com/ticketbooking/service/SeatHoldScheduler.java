package com.ticketbooking.service;

import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.EventSeatStatus;
import com.ticketbooking.repository.EventSeatRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class SeatHoldScheduler {

        private final EventSeatRepository eventSeatRepository;

        public SeatHoldScheduler(
                        EventSeatRepository eventSeatRepository) {

                this.eventSeatRepository = eventSeatRepository;
        }

        // =====================================================
        // AUTOMATICALLY RELEASE EXPIRED SEATS
        // =====================================================

        @Scheduled(fixedRate = 60000)
        @Transactional
        public void releaseExpiredSeats() {

                LocalDateTime now = LocalDateTime.now(
                                ZoneId.of("Asia/Kolkata"));

                List<EventSeat> expiredSeats = eventSeatRepository
                                .findByStatusAndHoldExpiresAtBefore(
                                                EventSeatStatus.HELD,
                                                now);

                for (EventSeat eventSeat : expiredSeats) {

                        eventSeat.setStatus(
                                        EventSeatStatus.AVAILABLE);

                        eventSeat.setHeldBy(null);

                        eventSeat.setHoldExpiresAt(null);

                        eventSeatRepository.save(eventSeat);

                        System.out.println(
                                        "Automatically released expired seat: "
                                                        + eventSeat.getId());
                }
        }
}