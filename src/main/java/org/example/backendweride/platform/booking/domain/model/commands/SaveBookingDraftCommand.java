package org.example.backendweride.platform.booking.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command to save a booking draft.
 *
 * @summary This command encapsulates ALL the data required to save a draft booking.
 */
public record SaveBookingDraftCommand(
    Long userId,
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
    Integer ratingScore,
    String ratingComment
) {
}
