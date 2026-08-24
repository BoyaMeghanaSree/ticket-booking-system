package com.ticketbooking.repository;

import com.ticketbooking.entity.Booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository
                extends JpaRepository<Booking, Long> {

        // =====================================================
        // GET USER BOOKINGS
        // =====================================================

        List<Booking> findByUserId(Long userId);

        // =====================================================
        // GET EVENT BOOKINGS
        // =====================================================

        List<Booking> findByEventId(Long eventId);
}