package org.example.backendweride.platform.booking.interfaces.resources;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

public record BookingResource(
    String id,
    String customerId,
    String vehicleId,
    String vehicleBrand,
    String vehicleModel,
    LocalDate date,
    LocalTime unlockTime,
    int durationMinutes,
    BigDecimal pricePerMinute,
    BigDecimal totalPrice,
    String status
) { }
