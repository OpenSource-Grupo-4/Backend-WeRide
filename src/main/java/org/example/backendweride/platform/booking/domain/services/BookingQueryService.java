package org.example.backendweride.platform.booking.domain.services;

import org.example.backendweride.platform.booking.domain.model.queries.*;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for handling booking-related queries.
 *
 * @summary This service manages read operations for bookings,
 *          including retrieving bookings by ID, customer, date range, and search criteria.
 */
public interface BookingQueryService {

    Optional<BookingResource> getBookingById(GetBookingByIdQuery query);

    Page<BookingResource> getBookingsByCustomer(GetBookingsByCustomerQuery query, Pageable pageable);

    Page<BookingResource> getBookingDraftsByCustomer(GetBookingDraftsByCustomerQuery query, Pageable pageable);

    Page<BookingResource> getBookingsByDateRange(GetBookingsByDateRangeQuery query, Pageable pageable);

    Page<BookingResource> searchBookings(SearchBookingsQuery query, Pageable pageable);

    Page<BookingResource> getAllBookings(GetAllBookingsQuery query, Pageable pageable);

    Page<BookingResource> getBookingsByVehicle(GetBookingsByVehicleQuery query, Pageable pageable);

    Page<BookingResource> getBookingsByStatus(GetBookingsByStatusQuery query, Pageable pageable);

    Page<BookingResource> getBookingsByUserId(GetBookingsByUserIdQuery query, Pageable pageable);

    Page<BookingResource> getPendingBookingsByUser(GetPendingBookingsByUserQuery query, Pageable pageable);

    Page<BookingResource> getCompletedBookingsByUser(GetCompletedBookingsByUserQuery query, Pageable pageable);
}
