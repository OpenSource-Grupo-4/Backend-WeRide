package org.example.backendweride.platform.booking.interfaces.resources;

/**
 * RatingResource record representing the rating information.
 *
 * @summary This record encapsulates the rating score and comment for a booking.
 */
public record RatingResource(
    Integer score,
    String comment
) {
}

