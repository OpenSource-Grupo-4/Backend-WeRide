package org.example.backendweride.platform.booking.interfaces.resources;

import java.time.LocalDateTime;

/**
 * UpdateBookingResource record representing the data a client may change on a booking.
 *
 * @summary Status, costs and payment status are managed server-side and therefore not
 *          part of this resource: they cannot be forged by the client. Location and
 *          timing fields can still be updated (subject to server validation).
 */
public record UpdateBookingResource(
        Long startLocationId,
        Long endLocationId,
        LocalDateTime reservedAt,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime actualStartDate,
        LocalDateTime actualEndDate,
        String paymentMethod,
        Double distance,
        Integer duration,
        Double averageSpeed,
        RatingResource rating
) {}
