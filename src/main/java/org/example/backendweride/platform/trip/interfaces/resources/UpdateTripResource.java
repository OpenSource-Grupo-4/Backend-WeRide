package org.example.backendweride.platform.trip.interfaces.resources;

import org.example.backendweride.platform.trip.domain.valueobjects.RouteCoordinates;

import java.util.Date;
import java.util.List;

public record UpdateTripResource(
        Long bookingId,
        Long vehicleId,
        Long startLocationId,
        Long endLocationId,
        String route,
        List<RouteCoordinates> routeCoordinates,
        Date startDate,
        Date endDate,
        Integer duration,
        Float distance,
        Float averageSpeed,
        Float maxSpeed,
        Float totalCost,
        Float carbonSaved,
        Integer caloriesBurned,
        String weather,
        Integer temperature,
        String status,
        List<String> incidentReports,
        List<String> photos
) {}
