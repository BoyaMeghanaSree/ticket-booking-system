package com.ticketbooking.config;

import com.ticketbooking.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(
                        JwtAuthenticationFilter jwtAuthenticationFilter) {

                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        // =========================
        // SECURITY FILTER CHAIN
        // =========================

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http

                                // =========================
                                // DISABLE CSRF
                                // =========================

                                .csrf(csrf -> csrf.disable())

                                // =========================
                                // ENABLE CORS
                                // =========================

                                .cors(cors -> cors.configurationSource(
                                                corsConfigurationSource()))

                                // =========================
                                // STATELESS SESSION
                                // =========================

                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                // =========================
                                // AUTHORIZATION
                                // =========================

                                .authorizeHttpRequests(auth -> auth

                                                .requestMatchers(
                                                                "/api/health",
                                                                "/api/auth/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                org.springframework.http.HttpMethod.GET,
                                                                "/api/waitlist/event/**")
                                                .permitAll()

                                                .anyRequest().authenticated())

                                // =========================
                                // DISABLE FORM LOGIN
                                // =========================

                                .formLogin(form -> form.disable())

                                // =========================
                                // DISABLE BASIC AUTH
                                // =========================

                                .httpBasic(basic -> basic.disable())

                                // =========================
                                // JWT FILTER
                                // =========================

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // =========================
        // CORS CONFIGURATION
        // =========================

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(
                                List.of(
                                                "http://localhost:5173",
                                                "https://ticket-booking-system-beige-ten.vercel.app"));

                configuration.setAllowedMethods(
                                List.of(
                                                "GET",
                                                "POST",
                                                "PUT",
                                                "DELETE",
                                                "OPTIONS"));

                configuration.setAllowedHeaders(
                                List.of("*"));

                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                configuration);

                return source;
        }
}