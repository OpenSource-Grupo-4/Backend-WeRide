package org.example.backendweride.platform.booking.interfaces.resources;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateBookingResource(
    String id,
    String customerId,
    String vehicleType, // el usuario elige tipo de vehiculo según el catálogo (id o type)
    LocalDate date,
    LocalTime unlockTime,
    int durationMinutes
) { }
