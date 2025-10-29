package org.example.backendweride.platform.booking.application.queryservice;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingEntity;
import org.example.backendweride.platform.booking.interfaces.transform.BookingResourceFromEntityAssembler;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.example.backendweride.platform.booking.domain.model.queries.SearchBookingsQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByDateRangeQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingsByCustomerQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingDraftsByCustomerQuery;
import org.example.backendweride.platform.booking.domain.model.queries.GetBookingByIdQuery;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingQueryServiceImpl {

    private final BookingRepository bookingRepository;

    public BookingQueryServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Optional<BookingResource> getBookingById(GetBookingByIdQuery q) {
        if (q == null || q.bookingId() == null) return Optional.empty();
        return bookingRepository.findById(q.bookingId()).map(BookingResourceFromEntityAssembler::toResource);
    }

    public Page<BookingResource> getBookingsByCustomer(GetBookingsByCustomerQuery q, Pageable pageable) {
        var page = bookingRepository.findByCustomerId(q.customerId(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    public Page<BookingResource> getBookingDraftsByCustomer(GetBookingDraftsByCustomerQuery q, Pageable pageable) {
        var page = bookingRepository.findByCustomerIdAndStatus(q.customerId(), "draft", pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    public Page<BookingResource> getBookingsByDateRange(GetBookingsByDateRangeQuery q, Pageable pageable) {
        var page = bookingRepository.findByDateBetween(q.from(), q.to(), pageable);
        return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
    }

    public Page<BookingResource> searchBookings(SearchBookingsQuery q, Pageable pageable) {
        // simple implementation: filter by provided fields using repository queries where possible
        if (q == null) return Page.empty(pageable);

        if (q.customerId() != null && q.status() != null) {
            var page = bookingRepository.findByCustomerIdAndStatus(q.customerId(), q.status(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.customerId() != null) {
            var page = bookingRepository.findByCustomerId(q.customerId(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.vehicleId() != null) {
            var page = bookingRepository.findByVehicleId(q.vehicleId(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.startAtFrom() != null && q.startAtTo() != null) {
            var page = bookingRepository.findByDateBetween(q.startAtFrom(), q.startAtTo(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        if (q.status() != null) {
            var page = bookingRepository.findByStatus(q.status(), pageable);
            return new PageImpl<>(page.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, page.getTotalElements());
        }

        // default: return all
        var all = bookingRepository.findAll(pageable);
        return new PageImpl<>(all.stream().map(BookingResourceFromEntityAssembler::toResource).collect(Collectors.toList()), pageable, all.getTotalElements());
    }

}
