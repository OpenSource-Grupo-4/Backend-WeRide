package org.example.backendweride.platform.booking.interfaces.resources;

import java.time.LocalDateTime;

/**
 * CreateBookingResource record representing the data the client may submit when creating a booking.
 *
 * @summary The user, booking status, costs and payment status are always derived server-side
 *          and are intentionally not part of this resource: a client cannot forge them.
 */
public record CreateBookingResource(
    Long vehicleId,
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
) { }
