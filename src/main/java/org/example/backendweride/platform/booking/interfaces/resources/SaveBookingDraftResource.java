package org.example.backendweride.platform.booking.interfaces.resources;

import java.time.LocalDateTime;

/**
 * SaveBookingDraftResource record representing the data the client may submit to save a draft.
 *
 * @summary The user, status, costs and payment status are derived server-side and are not
 *          part of this resource.
 */
public record SaveBookingDraftResource(
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
