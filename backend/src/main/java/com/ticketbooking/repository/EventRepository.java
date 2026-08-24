package com.ticketbooking.repository;

import com.ticketbooking.entity.Event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository
        extends JpaRepository<Event, Long> {

    List<Event> findByOrganiserId(Long organiserId);
}