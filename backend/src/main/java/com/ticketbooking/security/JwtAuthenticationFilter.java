package com.ticketbooking.security;

import com.ticketbooking.entity.User;
import com.ticketbooking.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // =========================
    // SKIP AUTH ENDPOINTS
    // =========================

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path = request.getServletPath();

        return path.startsWith("/api/auth/");
    }

    // =========================
    // JWT FILTER
    // =========================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;

        // =========================
        // GET JWT TOKEN
        // =========================

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            token = authHeader.substring(7);

            try {

                email = jwtService.extractEmail(token);

            } catch (Exception e) {

                System.out.println(
                        "Invalid JWT token: "
                                + e.getMessage());
            }
        }

        // =========================
        // AUTHENTICATE USER
        // =========================

        if (email != null
                && SecurityContextHolder
                        .getContext()
                        .getAuthentication() == null) {

            try {

                User user = userRepository
                        .findByEmail(email)
                        .orElse(null);

                if (user != null
                        && jwtService.isTokenValid(token)) {

                    // =========================
                    // CREATE USER DETAILS
                    // =========================

                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(user.getEmail())
                            .password(user.getPassword())
                            .authorities(
                                    new SimpleGrantedAuthority(
                                            "ROLE_"
                                                    + user.getRole().name()))
                            .build();

                    // =========================
                    // CREATE AUTHENTICATION
                    // =========================

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication);

                    System.out.println(
                            "JWT authenticated user: "
                                    + user.getEmail());
                }

            } catch (Exception e) {

                System.out.println(
                        "JWT authentication failed: "
                                + e.getMessage());
            }
        }

        // =========================
        // CONTINUE REQUEST
        // =========================

        filterChain.doFilter(
                request,
                response);
    }
}