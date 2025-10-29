package org.example.backendweride.platform.booking.domain.model.queries;

public record GetBookingsByCustomerQuery(
    String customerId,
    int page,
    int size
) {
}
