package com.ticketbooking.service;

import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.EventSeat;
import com.ticketbooking.entity.Waitlist;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

        private final JavaMailSender mailSender;
        private final QrCodeService qrCodeService;

        @Value("${app.frontend.url:http://localhost:5173}")
        private String frontendUrl;

        public EmailService(
                        JavaMailSender mailSender,
                        QrCodeService qrCodeService) {

                this.mailSender = mailSender;
                this.qrCodeService = qrCodeService;
        }

        // =====================================================
        // BOOKING CONFIRMATION EMAIL
        // =====================================================

        public void sendBookingConfirmation(Booking booking) {

                try {

                        String bookingReference = "BOOKING-" + booking.getId();

                        // Generate QR
                        byte[] qrCode = qrCodeService.generateQrCode(
                                        bookingReference);

                        String customerEmail = booking.getUser().getEmail();

                        String eventTitle = booking.getEvent().getTitle();

                        EventSeat eventSeat = booking.getEventSeat();

                        String seatNumber = eventSeat.getSeat().getSeatNumber();

                        String category = eventSeat.getSeat()
                                        .getCategory()
                                        .toString();

                        String bookedAt = booking.getBookedAt()
                                        .format(
                                                        DateTimeFormatter.ofPattern(
                                                                        "dd-MM-yyyy HH:mm"));

                        MimeMessage message = mailSender.createMimeMessage();

                        MimeMessageHelper helper = new MimeMessageHelper(
                                        message,
                                        true);

                        // IMPORTANT
                        helper.setFrom(
                                        System.getenv("MAIL_USERNAME"));

                        helper.setTo(customerEmail);

                        helper.setSubject(
                                        "Ticket Booking Confirmation - "
                                                        + bookingReference);

                        String emailBody = "<html>"
                                        + "<body>"
                                        + "<h2>Ticket Booking Confirmed</h2>"

                                        + "<p>Dear "
                                        + booking.getUser().getName()
                                        + ",</p>"

                                        + "<p>Your ticket has been "
                                        + "successfully booked.</p>"

                                        + "<hr>"

                                        + "<p><b>Booking Reference:</b> "
                                        + bookingReference
                                        + "</p>"

                                        + "<p><b>Event:</b> "
                                        + eventTitle
                                        + "</p>"

                                        + "<p><b>Seat:</b> "
                                        + seatNumber
                                        + "</p>"

                                        + "<p><b>Category:</b> "
                                        + category
                                        + "</p>"

                                        + "<p><b>Booked At:</b> "
                                        + bookedAt
                                        + "</p>"

                                        + "<p><b>Price:</b> ₹"
                                        + booking.getPrice()
                                        + "</p>"

                                        + "<p>The QR code ticket "
                                        + "is attached to this email.</p>"

                                        + "</body>"
                                        + "</html>";

                        helper.setText(
                                        emailBody,
                                        true);

                        // Attach QR
                        helper.addAttachment(
                                        bookingReference + ".png",
                                        new ByteArrayResource(qrCode));

                        // SEND
                        mailSender.send(message);

                        System.out.println(
                                        "====================================");

                        System.out.println(
                                        "BOOKING EMAIL SENT SUCCESSFULLY");

                        System.out.println(
                                        "To: " + customerEmail);

                        System.out.println(
                                        "QR Attachment: "
                                                        + bookingReference
                                                        + ".png");

                        System.out.println(
                                        "====================================");

                } catch (Exception e) {

                        System.err.println(
                                        "====================================");

                        System.err.println(
                                        "BOOKING EMAIL FAILED");

                        e.printStackTrace();

                        System.err.println(
                                        "====================================");

                        throw new RuntimeException(
                                        "Failed to send booking confirmation email",
                                        e);
                }
        }
        // =====================================================
        // WAITLIST OFFER EMAIL
        // =====================================================

        public void sendWaitlistOfferEmail(
                        Waitlist waitlist) {

                try {

                        if (waitlist.getUser() == null) {
                                return;
                        }

                        if (waitlist.getOfferedSeat() == null) {
                                return;
                        }

                        String customerEmail = waitlist.getUser().getEmail();

                        String customerName = waitlist.getUser().getName();

                        String eventTitle = waitlist.getEvent().getTitle();

                        EventSeat eventSeat = waitlist.getOfferedSeat();

                        String seatNumber = eventSeat.getSeat()
                                        .getSeatNumber();

                        String category = eventSeat.getSeat()
                                        .getCategory()
                                        .toString();

                        String expiry = waitlist.getOfferExpiresAt()
                                        .format(
                                                        DateTimeFormatter.ofPattern(
                                                                        "dd-MM-yyyy HH:mm"));

                        // =================================================
                        // FRONTEND OFFER LINK
                        // =================================================

                        String offerLink = frontendUrl
                                        + "/waitlist-offer/"
                                        + waitlist.getId();

                        MimeMessage message = mailSender.createMimeMessage();

                        MimeMessageHelper helper = new MimeMessageHelper(
                                        message,
                                        true);

                        helper.setTo(customerEmail);

                        helper.setSubject(
                                        "Seat Available - "
                                                        + eventTitle);

                        String emailBody = "<html>"
                                        + "<body>"

                                        + "<h2>Seat Available!</h2>"

                                        + "<p>Dear "
                                        + customerName
                                        + ",</p>"

                                        + "<p>A seat has become available "
                                        + "for an event on your waitlist.</p>"

                                        + "<hr>"

                                        + "<p><b>Event:</b> "
                                        + eventTitle
                                        + "</p>"

                                        + "<p><b>Seat:</b> "
                                        + seatNumber
                                        + "</p>"

                                        + "<p><b>Category:</b> "
                                        + category
                                        + "</p>"

                                        + "<p><b>Offer expires:</b> "
                                        + expiry
                                        + "</p>"

                                        + "<p>You have a limited time "
                                        + "to complete this booking.</p>"

                                        + "<p>"
                                        + "<a href=\""
                                        + offerLink
                                        + "\" "
                                        + "style=\"background:#2563eb;"
                                        + "color:white;"
                                        + "padding:12px 20px;"
                                        + "text-decoration:none;"
                                        + "border-radius:6px;\">"
                                        + "Accept Seat Offer"
                                        + "</a>"
                                        + "</p>"

                                        + "<p>If you do not complete the "
                                        + "booking before the offer expires, "
                                        + "the seat will be offered to the "
                                        + "next customer on the waitlist.</p>"

                                        + "<p>Thank you for using "
                                        + "Ticket Booking.</p>"

                                        + "</body>"
                                        + "</html>";

                        helper.setText(
                                        emailBody,
                                        true);

                        mailSender.send(message);

                } catch (MessagingException e) {

                        throw new RuntimeException(
                                        "Failed to send waitlist offer email",
                                        e);
                }
        }
}