package com.weride.platform.garage.interfaces.rest.controllers;

import com.weride.platform.garage.application.internal.commandservices.AddFavoriteCommandService;
import com.weride.platform.garage.application.internal.queryservices.VehicleQueryServiceImpl;
import com.weride.platform.garage.domain.model.aggregates.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleQueryServiceImpl vehicleQueryService;
    private final AddFavoriteCommandService addFavoriteCommandService;

    @GetMapping
    public List<Vehicle> getAll() { return vehicleQueryService.handle(); }

    @PostMapping("/{id}/favorite")
    public Vehicle markAsFavorite(@PathVariable Long id) {
        return addFavoriteCommandService.handle(id);
    }
}
