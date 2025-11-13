package org.example.backendweride.platform.booking.application.commandservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.backendweride.platform.booking.domain.services.BookingCommandService;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;
import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingEntity;
import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingRepository;
import org.example.backendweride.platform.booking.domain.services.VehicleCatalogService;
import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;

import java.math.BigDecimal;

/**
 * Implementation of BookingCommandService to handle booking-related commands.
 *
 * @summary This service processes commands for saving booking drafts and creating bookings.
 */
@Service
@Transactional
public class BookingCommandServiceImpl implements BookingCommandService {

    private final BookingRepository bookingRepository;
    private final VehicleCatalogService vehicleCatalogService;

    public BookingCommandServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleCatalogService = new VehicleCatalogService();
    }

    @Override
    public SaveDraftResult saveDraft(SaveBookingDraftCommand command) {
        try {
            var vehicleOpt = vehicleCatalogService.findById(command.vehicleId());
            if (vehicleOpt.isEmpty()) {
                return new SaveDraftResult(null, false, "Vehicle not found");
            }
            BigDecimal pricePerMinute = vehicleOpt.get().pricePerMinute();

            Booking booking = Booking.createDraftFrom(command, pricePerMinute);
            BookingEntity entity = booking.toEntity();
            bookingRepository.save(entity);

            return new SaveDraftResult(booking.getId(), true, "Draft saved");
        } catch (Exception e) {
            return new SaveDraftResult(null, false, "Failed to save draft: " + e.getMessage());
        }
    }

    @Override
    public CreateBookingResult createBooking(CreateBookingCommand command) {
        try {
            var vehicleOpt = vehicleCatalogService.findById(command.vehicleId());
            if (vehicleOpt.isEmpty()) {
                return new CreateBookingResult(null, false, "Vehicle not found");
            }
            BigDecimal pricePerMinute = vehicleOpt.get().pricePerMinute();

            Booking booking = Booking.createConfirmedFrom(command, pricePerMinute);
            BookingEntity entity = booking.toEntity();
            bookingRepository.save(entity);

            return new CreateBookingResult(booking.getId(), true, "Booking created");
        } catch (Exception e) {
            return new CreateBookingResult(null, false, "Failed to create booking: " + e.getMessage());
        }
    }
}
