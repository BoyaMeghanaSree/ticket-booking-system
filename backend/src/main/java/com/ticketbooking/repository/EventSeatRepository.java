package com.ticketbooking.repository;

import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.EventSeatStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventSeatRepository
                extends JpaRepository<EventSeat, Long> {

        List<EventSeat> findByEventId(Long eventId);

        List<EventSeat> findByStatus(
                        EventSeatStatus status);

        List<EventSeat> findByStatusAndHoldExpiresAtBefore(
                        EventSeatStatus status,
                        LocalDateTime time);

        Optional<EventSeat> findByEventIdAndSeatId(
                        Long eventId,
                        Long seatId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT e FROM EventSeat e WHERE e.id = :id")
        Optional<EventSeat> findByIdWithLock(
                        @Param("id") Long id);
}