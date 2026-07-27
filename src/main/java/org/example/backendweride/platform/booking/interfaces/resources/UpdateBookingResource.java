package org.example.backendweride.platform.booking.interfaces.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateBookingResource(
        Long vehicleId,
        Long startLocationId,
        Long endLocationId,
        LocalDateTime reservedAt,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime actualStartDate,
        LocalDateTime actualEndDate,
        String status,
        BigDecimal totalCost,
        BigDecimal discount,
        BigDecimal finalCost,
        String paymentMethod,
        String paymentStatus,
        Double distance,
        Integer duration,
        Double averageSpeed,
        RatingResource rating
) {}
