package org.example.backendweride.platform.booking.domain.model.queries;

public record GetBookingDraftsByCustomerQuery(
    String customerId,
    int page,
    int size
) {
}
