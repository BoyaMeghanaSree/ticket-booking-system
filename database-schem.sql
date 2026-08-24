-- ============================================================
-- TICKET BOOKING SYSTEM DATABASE SCHEMA
-- ============================================================
--
-- Database:
-- ticket_booking
--
-- Main tables:
-- users
-- venues
-- seats
-- events
-- event_seats
-- bookings
-- waitlist
--
-- IMPORTANT:
-- This file documents the database structure used by the
-- application. The application uses JPA/Hibernate mappings
-- to manage the schema.
--
-- For an exact database export, use:
--
-- MySQL Workbench
-- Server → Data Export
--
-- Select ticket_booking database
-- Select all required tables
-- Export to Self-Contained File
--
-- ============================================================

CREATE DATABASE IF NOT EXISTS ticket_booking;

USE ticket_booking;

-- ============================================================
-- TABLES USED BY THE APPLICATION
-- ============================================================

-- users
-- Stores customer, organiser and admin accounts.

-- venues
-- Stores venue information.

-- seats
-- Stores the physical seats belonging to a venue.

-- events
-- Stores movie/concert event information, prices, date,
-- time and venue.

-- event_seats
-- Stores the seat state for a particular event.
--
-- Important states:
-- AVAILABLE
-- HELD
-- BOOKED

-- bookings
-- Stores confirmed and cancelled ticket bookings.

-- waitlist
-- Stores customers waiting for seats in a particular
-- category.
--
-- Important states:
-- WAITING
-- OFFERED
-- COMPLETED
-- EXPIRED

-- ============================================================
-- RELATIONSHIP OVERVIEW
-- ============================================================

-- users
--   ├── bookings
--   └── waitlist
--
-- venues
--   └── seats
--
-- events
--   ├── event_seats
--   ├── bookings
--   └── waitlist
--
-- seats
--   └── event_seats

-- ============================================================
-- NOTE
-- ============================================================
--
-- The exact CREATE TABLE statements should be exported from
-- the running MySQL database using MySQL Workbench so that
-- generated constraint and foreign-key names remain identical
-- to the application database.
--
-- ============================================================