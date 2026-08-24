package com.ticketbooking.repository;

import com.ticketbooking.entity.Waitlist;
import com.ticketbooking.entity.WaitlistStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository
                extends JpaRepository<Waitlist, Long> {

        // Get all waitlist entries for an event
        List<Waitlist> findByEventId(Long eventId);

        // Check whether user is already on waitlist
        Optional<Waitlist> findByEventIdAndUserIdAndCategory(
                        Long eventId,
                        Long userId,
                        com.ticketbooking.entity.SeatCategory category);

        // Get first waiting customer in queue
        Optional<Waitlist> findFirstByEventIdAndCategoryAndStatusOrderByJoinedAtAscIdAsc(
                        Long eventId,
                        com.ticketbooking.entity.SeatCategory category,
                        WaitlistStatus status);

        // Find expired offers
        List<Waitlist> findByStatusAndOfferExpiresAtBefore(
                        WaitlistStatus status,
                        LocalDateTime time);
}