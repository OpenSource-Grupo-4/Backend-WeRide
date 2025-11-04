package org.example.backendweride.platform.booking.interfaces.resources;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * CreateBookingResource record representing the data needed to create a new booking.
 *
 * @summary This record encapsulates the necessary information for creating a booking,
 *          including customer ID, vehicle type, date, unlock time, and duration.
 */
public record CreateBookingResource(
    String id,
    String customerId,
    String vehicleType, // The user choose the type of vehicle they want to book
    LocalDate date,
    LocalTime unlockTime,
    int durationMinutes
) { }
