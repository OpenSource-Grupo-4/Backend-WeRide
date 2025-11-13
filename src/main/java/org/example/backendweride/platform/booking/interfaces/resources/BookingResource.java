package org.example.backendweride.platform.booking.interfaces.resources;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

/**
 * BookingResource record representing booking details.
 *
 * @summary This record encapsulates the details of a booking including customer
 * and vehicle information, timing, pricing, and status.
 *
 */
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
