package com.ticketbooking.service;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.BookingStatus;
import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.EventSeatStatus;
import com.ticketbooking.entity.SeatCategory;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.Waitlist;
import com.ticketbooking.entity.WaitlistStatus;

import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.EventSeatRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.WaitlistRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class WaitlistService {

        private final WaitlistRepository waitlistRepository;
        private final EventRepository eventRepository;
        private final UserRepository userRepository;
        private final EventSeatRepository eventSeatRepository;
        private final BookingRepository bookingRepository;
        private final EmailService emailService;

        public WaitlistService(
                        WaitlistRepository waitlistRepository,
                        EventRepository eventRepository,
                        UserRepository userRepository,
                        EventSeatRepository eventSeatRepository,
                        BookingRepository bookingRepository,
                        EmailService emailService) {

                this.waitlistRepository = waitlistRepository;
                this.eventRepository = eventRepository;
                this.userRepository = userRepository;
                this.eventSeatRepository = eventSeatRepository;
                this.bookingRepository = bookingRepository;
                this.emailService = emailService;
        }

        // =====================================================
        // GET EVENT WAITLIST
        // =====================================================

        public List<Waitlist> getEventWaitlist(
                        Long eventId) {

                return waitlistRepository
                                .findByEventId(eventId);
        }

        // =====================================================
        // GET WAITLIST BY ID
        // =====================================================

        public Waitlist getWaitlistById(
                        Long waitlistId) {

                return waitlistRepository
                                .findById(waitlistId)
                                .orElse(null);
        }

        // =====================================================
        // JOIN WAITLIST
        // =====================================================

        @Transactional
        public Waitlist joinWaitlist(
                        Long eventId,
                        Long userId,
                        SeatCategory category) {

                // -------------------------------------------------
                // GET EVENT
                // -------------------------------------------------

                Event event = eventRepository
                                .findById(eventId)
                                .orElse(null);

                if (event == null) {

                        throw new RuntimeException(
                                        "Event not found");
                }

                // -------------------------------------------------
                // GET USER
                // -------------------------------------------------

                User user = userRepository
                                .findById(userId)
                                .orElse(null);

                if (user == null) {

                        throw new RuntimeException(
                                        "User not found");
                }

                // -------------------------------------------------
                // CHECK EXISTING WAITLIST
                // -------------------------------------------------

                Optional<Waitlist> existing = waitlistRepository
                                .findByEventIdAndUserIdAndCategory(
                                                eventId,
                                                userId,
                                                category);

                if (existing.isPresent()) {

                        Waitlist oldWaitlist = existing.get();

                        if (oldWaitlist.getStatus() == WaitlistStatus.WAITING
                                        ||
                                        oldWaitlist.getStatus() == WaitlistStatus.OFFERED) {

                                throw new RuntimeException(
                                                "You are already on the waitlist");
                        }
                }

                // -------------------------------------------------
                // CREATE WAITLIST ENTRY
                // -------------------------------------------------

                Waitlist waitlist = new Waitlist(
                                event,
                                user,
                                category);

                return waitlistRepository.save(
                                waitlist);
        }

        // =====================================================
        // OFFER SEAT TO NEXT CUSTOMER
        // =====================================================

        @Transactional
        public void offerSeatToNextCustomer(
                        EventSeat eventSeat) {

                // -------------------------------------------------
                // GET CATEGORY
                // -------------------------------------------------

                SeatCategory category = eventSeat
                                .getSeat()
                                .getCategory();

                // -------------------------------------------------
                // GET EVENT ID
                // -------------------------------------------------

                Long eventId = eventSeat
                                .getEvent()
                                .getId();

                // -------------------------------------------------
                // FIND NEXT WAITING CUSTOMER
                // -------------------------------------------------

                Waitlist nextCustomer = waitlistRepository
                                .findFirstByEventIdAndCategoryAndStatusOrderByJoinedAtAscIdAsc(
                                                eventId,
                                                category,
                                                WaitlistStatus.WAITING)
                                .orElse(null);

                // -------------------------------------------------
                // NO WAITLIST CUSTOMER
                // -------------------------------------------------

                if (nextCustomer == null) {

                        eventSeat.setStatus(
                                        EventSeatStatus.AVAILABLE);

                        eventSeat.setHeldBy(null);

                        eventSeat.setHoldExpiresAt(null);

                        eventSeatRepository.save(
                                        eventSeat);

                        return;
                }

                // -------------------------------------------------
                // CREATE 10-MINUTE OFFER
                // -------------------------------------------------

                LocalDateTime now = LocalDateTime.now(
                                ZoneId.of("Asia/Kolkata"));

                LocalDateTime expiry = now.plusMinutes(10);

                // -------------------------------------------------
                // UPDATE WAITLIST
                // -------------------------------------------------

                nextCustomer.setStatus(
                                WaitlistStatus.OFFERED);

                nextCustomer.setOfferedSeat(
                                eventSeat);

                nextCustomer.setOfferExpiresAt(
                                expiry);

                // -------------------------------------------------
                // HOLD SEAT
                // -------------------------------------------------

                eventSeat.setStatus(
                                EventSeatStatus.HELD);

                eventSeat.setHeldBy(
                                nextCustomer.getUser());

                eventSeat.setHoldExpiresAt(
                                expiry);

                waitlistRepository.save(
                                nextCustomer);

                eventSeatRepository.save(
                                eventSeat);

                // -------------------------------------------------
                // SEND WAITLIST EMAIL
                // -------------------------------------------------

                try {

                        emailService.sendWaitlistOfferEmail(
                                        nextCustomer);

                        System.out.println(
                                        "Waitlist offer email sent to "
                                                        + nextCustomer
                                                                        .getUser()
                                                                        .getEmail());

                } catch (Exception e) {

                        System.out.println(
                                        "Unable to send waitlist offer email: "
                                                        + e.getMessage());
                }

                System.out.println(
                                "Seat "
                                                + eventSeat.getId()
                                                + " offered to waitlist customer "
                                                + nextCustomer.getId()
                                                + " until "
                                                + expiry);
        }

        // =====================================================
        // ACCEPT WAITLIST OFFER
        // =====================================================

        @Transactional
        public Booking acceptOffer(
                        Long waitlistId,
                        Long userId) {

                // -------------------------------------------------
                // GET WAITLIST
                // -------------------------------------------------

                Waitlist waitlist = waitlistRepository
                                .findById(waitlistId)
                                .orElse(null);

                if (waitlist == null) {

                        throw new RuntimeException(
                                        "Waitlist entry not found");
                }

                // -------------------------------------------------
                // CHECK USER
                // -------------------------------------------------

                if (!waitlist
                                .getUser()
                                .getId()
                                .equals(userId)) {

                        throw new RuntimeException(
                                        "This offer does not belong to you");
                }

                // -------------------------------------------------
                // CHECK STATUS
                // -------------------------------------------------

                if (waitlist.getStatus() != WaitlistStatus.OFFERED) {

                        throw new RuntimeException(
                                        "No active offer exists");
                }

                // -------------------------------------------------
                // CHECK EXPIRY
                // -------------------------------------------------

                LocalDateTime now = LocalDateTime.now(
                                ZoneId.of("Asia/Kolkata"));

                if (waitlist.getOfferExpiresAt() == null
                                ||
                                !waitlist
                                                .getOfferExpiresAt()
                                                .isAfter(now)) {

                        EventSeat expiredSeat = waitlist.getOfferedSeat();

                        waitlist.setStatus(
                                        WaitlistStatus.EXPIRED);

                        waitlist.setOfferExpiresAt(null);

                        waitlist.setOfferedSeat(null);

                        waitlistRepository.save(
                                        waitlist);

                        if (expiredSeat != null) {

                                expiredSeat.setStatus(
                                                EventSeatStatus.AVAILABLE);

                                expiredSeat.setHeldBy(null);

                                expiredSeat.setHoldExpiresAt(null);

                                eventSeatRepository.save(
                                                expiredSeat);

                                // Give seat to next customer
                                offerSeatToNextCustomer(
                                                expiredSeat);
                        }

                        throw new RuntimeException(
                                        "Waitlist offer has expired");
                }

                // -------------------------------------------------
                // GET OFFERED SEAT
                // -------------------------------------------------

                EventSeat eventSeat = waitlist.getOfferedSeat();

                if (eventSeat == null) {

                        throw new RuntimeException(
                                        "No seat is attached to this offer");
                }

                // -------------------------------------------------
                // CALCULATE PRICE
                // -------------------------------------------------

                Double price;

                if (eventSeat
                                .getSeat()
                                .getCategory() == SeatCategory.PREMIUM) {

                        price = eventSeat
                                        .getEvent()
                                        .getPremiumPrice();

                } else {

                        price = eventSeat
                                        .getEvent()
                                        .getStandardPrice();
                }

                // -------------------------------------------------
                // BOOK SEAT
                // -------------------------------------------------

                eventSeat.setStatus(
                                EventSeatStatus.BOOKED);

                eventSeat.setHeldBy(null);

                eventSeat.setHoldExpiresAt(null);

                eventSeatRepository.save(
                                eventSeat);

                // -------------------------------------------------
                // MARK WAITLIST COMPLETED
                // -------------------------------------------------

                waitlist.setStatus(
                                WaitlistStatus.COMPLETED);

                waitlist.setOfferExpiresAt(null);

                waitlist.setOfferedSeat(null);

                waitlistRepository.save(
                                waitlist);

                // -------------------------------------------------
                // CREATE BOOKING
                // -------------------------------------------------

                Booking booking = new Booking(
                                eventSeat.getEvent(),
                                eventSeat,
                                waitlist.getUser(),
                                BookingStatus.CONFIRMED,
                                now,
                                price);

                Booking savedBooking = bookingRepository.save(
                                booking);

                // -------------------------------------------------
                // SEND BOOKING EMAIL
                // -------------------------------------------------

                try {

                        emailService.sendBookingConfirmation(
                                        savedBooking);

                } catch (Exception e) {

                        System.out.println(
                                        "Unable to send booking email: "
                                                        + e.getMessage());
                }

                return savedBooking;
        }

        // =====================================================
        // AUTOMATIC WAITLIST OFFER EXPIRY
        // =====================================================

        @Scheduled(fixedRate = 60000)
        @Transactional
        public void expireWaitlistOffers() {

                LocalDateTime now = LocalDateTime.now(
                                ZoneId.of("Asia/Kolkata"));

                // -------------------------------------------------
                // FIND EXPIRED OFFERS
                // -------------------------------------------------

                List<Waitlist> expiredOffers = waitlistRepository
                                .findByStatusAndOfferExpiresAtBefore(
                                                WaitlistStatus.OFFERED,
                                                now);

                // -------------------------------------------------
                // PROCESS EACH EXPIRED OFFER
                // -------------------------------------------------

                for (Waitlist waitlist : expiredOffers) {

                        EventSeat expiredSeat = waitlist.getOfferedSeat();

                        // ---------------------------------------------
                        // MARK OFFER EXPIRED
                        // ---------------------------------------------

                        waitlist.setStatus(
                                        WaitlistStatus.EXPIRED);

                        waitlist.setOfferExpiresAt(null);

                        waitlist.setOfferedSeat(null);

                        waitlistRepository.save(
                                        waitlist);

                        // ---------------------------------------------
                        // RELEASE SEAT
                        // ---------------------------------------------

                        if (expiredSeat != null) {

                                expiredSeat.setStatus(
                                                EventSeatStatus.AVAILABLE);

                                expiredSeat.setHeldBy(null);

                                expiredSeat.setHoldExpiresAt(null);

                                eventSeatRepository.save(
                                                expiredSeat);

                                // -----------------------------------------
                                // OFFER TO NEXT CUSTOMER
                                // -----------------------------------------

                                offerSeatToNextCustomer(
                                                expiredSeat);
                        }

                        System.out.println(
                                        "Waitlist offer expired for waitlist ID: "
                                                        + waitlist.getId());
                }
        }
}