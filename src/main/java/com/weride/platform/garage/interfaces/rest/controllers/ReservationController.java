package com.weride.platform.garage.interfaces.rest.controllers;

import com.weride.platform.garage.application.internal.commandservices.CreateReservationCommandService;
import com.weride.platform.garage.application.internal.queryservices.ReservationQueryServiceImpl;
import com.weride.platform.garage.domain.model.aggregates.Reservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Gestión de reservas de vehículos")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationQueryServiceImpl reservationQueryService;
    private final CreateReservationCommandService createReservationCommandService;

    @GetMapping
    public List<Reservation> getAll() { return reservationQueryService.handle(); }

    @PostMapping
    @Operation(
            summary = "Crear reserva",
            description = "Crea una nueva reserva para un vehículo",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
                    @ApiResponse(responseCode = "400", description = "Datos inválidos")
            }
    )
    public Reservation create(@RequestBody Reservation reservation) {
        return createReservationCommandService.handle(reservation);
    }
}
