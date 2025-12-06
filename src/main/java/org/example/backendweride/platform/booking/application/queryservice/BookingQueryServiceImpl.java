package org.example.backendweride.platform.booking.application.queryservice;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.example.backendweride.platform.booking.domain.services.BookingQueryService;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.example.backendweride.platform.booking.interfaces.transform.BookingResourceFromEntityAssembler;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.example.backendweride.platform.booking.domain.model.queries.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service to handle booking-related queries.
 *
 * @summary This service retrieves booking information based on provided queries.
 */
@Service
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;

    public BookingQueryServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Optional<BookingResource> getBookingById(GetBookingByIdQuery q) {
        if (q == null || q.bookingId() == null) return Optional.empty();
        return bookingRepository.findByBookingId(q.bookingId()).map(BookingResourceFromEntityAssembler::toResource);
    }

    @Override
    public Page<BookingResource> getBookingsByCustomer(GetBookingsByCustomerQuery q, Pageable pageable) {
        var page = bookingRepository.findByUserId(q.customerId(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public Page<BookingResource> getBookingDraftsByCustomer(GetBookingDraftsByCustomerQuery q, Pageable pageable) {
        var page = bookingRepository.findByUserIdAndStatus(q.customerId(), "draft", pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public Page<BookingResource> getBookingsByDateRange(GetBookingsByDateRangeQuery q, Pageable pageable) {
        LocalDateTime from = q.from().atStartOfDay();
        LocalDateTime to = q.to().atTime(23, 59, 59);
        var page = bookingRepository.findByStartDateBetween(from, to, pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public Page<BookingResource> searchBookings(SearchBookingsQuery q, Pageable pageable) {
        if (q == null) return Page.empty(pageable);

        if (q.customerId() != null && q.status() != null) {
            var page = bookingRepository.findByUserIdAndStatus(q.customerId(), q.status(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.customerId() != null) {
            var page = bookingRepository.findByUserId(q.customerId(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.vehicleId() != null) {
            var page = bookingRepository.findByVehicleId(q.vehicleId(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.startAtFrom() != null && q.startAtTo() != null) {
            LocalDateTime from = q.startAtFrom().atStartOfDay();
            LocalDateTime to = q.startAtTo().atTime(23, 59, 59);
            var page = bookingRepository.findByStartDateBetween(from, to, pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.status() != null) {
            var page = bookingRepository.findByStatus(q.status(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        var all = bookingRepository.findAll(pageable);
        return new PageImpl<>(all.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, all.getTotalElements());
    }

    @Override
    public Page<BookingResource> getAllBookings(GetAllBookingsQuery q, Pageable pageable) {
        var all = bookingRepository.findAll(pageable);
        return new PageImpl<>(all.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, all.getTotalElements());
    }

    @Override
    public Page<BookingResource> getBookingsByVehicle(GetBookingsByVehicleQuery q, Pageable pageable) {
        var page = bookingRepository.findByVehicleId(q.vehicleId(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public Page<BookingResource> getBookingsByStatus(GetBookingsByStatusQuery q, Pageable pageable) {
        var page = bookingRepository.findByStatus(q.status(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public Page<BookingResource> getBookingsByUserId(GetBookingsByUserIdQuery q, Pageable pageable) {
        var page = bookingRepository.findByUserId(q.userId(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    @Override
    public Page<BookingResource> getPendingBookingsByUser(GetPendingBookingsByUserQuery q, Pageable pageable) {
        // Get bookings with status 'pending' or 'confirmed'
        var pendingPage = bookingRepository.findByUserIdAndStatus(q.userId(), "pending", pageable);
        var confirmedPage = bookingRepository.findByUserIdAndStatus(q.userId(), "confirmed", pageable);

        // Combine both results
        var combined = new java.util.ArrayList<>(pendingPage.getContent());
        combined.addAll(confirmedPage.getContent());

        return new PageImpl<>(
            combined.stream()
                .map(BookingResourceFromEntityAssembler::toResource)
                .collect(Collectors.toList()),
            pageable,
            pendingPage.getTotalElements() + confirmedPage.getTotalElements()
        );
    }

    @Override
    public Page<BookingResource> getCompletedBookingsByUser(GetCompletedBookingsByUserQuery q, Pageable pageable) {
        var page = bookingRepository.findByUserIdAndStatus(q.userId(), "completed", pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }
}
