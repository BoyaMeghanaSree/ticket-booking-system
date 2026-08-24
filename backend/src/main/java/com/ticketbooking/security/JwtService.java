package com.ticketbooking.security;

import com.ticketbooking.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET = "TicketBookingSystemSecretKeyForJWTAuthentication123456789";

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(User user) {

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {

        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {

        try {
            Claims claims = extractClaims(token);

            return claims.getExpiration()
                    .after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}