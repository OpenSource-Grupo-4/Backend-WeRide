package org.example.backendweride.platform.booking.domain.model.exceptions;

/**
 * Thrown when a booking cannot be created/updated because it conflicts with an
 * existing booking (vehicle already reserved for the requested time range).
 */
public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
