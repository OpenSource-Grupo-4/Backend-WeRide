package org.example.backendweride.platform.booking.application.commandservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.VehicleRepository;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;

import java.math.BigDecimal;

/**
 * Implementation of BookingCommandService to handle booking-related commands.
 *
 * @summary This service processes commands for saving booking drafts and creating bookings.
 */
@Service
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;

    public BookingCommandServiceImpl(BookingRepository bookingRepository, VehicleRepository vehicleRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public SaveDraftResult saveDraft(SaveBookingDraftCommand command) {
        var vehicleOpt = vehicleRepository.findById(command.vehicleId());
        if (vehicleOpt.isEmpty()) {
            return new SaveDraftResult(null, false, "Vehicle not found");
        }

        Booking booking = Booking.createDraftFrom(command);

        // Calculate cost based on vehicle price per minute
        BigDecimal pricePerMinute = BigDecimal.valueOf(vehicleOpt.get().getPricePerMinute());
        booking.calculateCost(pricePerMinute);

        Booking saved = bookingRepository.save(booking);

        return new SaveDraftResult(saved.getBookingId(), true, "Draft saved");
    }

    @Override
    @Transactional
    public CreateBookingResult createBooking(CreateBookingCommand command) {
        Booking booking = Booking.createConfirmedFrom(command);
        Booking saved = bookingRepository.save(booking);

        return new CreateBookingResult(saved.getBookingId(), true, "Booking created");
    }
}
