package org.example.backendweride.platform.garage.interfaces.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.garage.application.internal.commandservices.AddFavoriteCommandService;
import org.example.backendweride.platform.garage.application.internal.commandservices.RemoveFavoriteCommandService;
import org.example.backendweride.platform.garage.application.internal.commandservices.VehicleCommandService;
import org.example.backendweride.platform.garage.application.internal.queryservices.VehicleQueryServiceImpl;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Vehicle Controller
 *
 * @summary Handles HTTP requests related to vehicle management.
 */
@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Vehicle", description = "Obtain and Update Vehicle Information")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleQueryServiceImpl vehicleQueryService;
    private final AddFavoriteCommandService addFavoriteCommandService;
    private final VehicleCommandService vehicleCommandService;
    private final RemoveFavoriteCommandService removeFavoriteCommandService;

    /**
     * Get all vehicles.
     *
     * @return ResponseEntity containing the list of all vehicles or a not found status.
     */
    @GetMapping
    @Operation(summary = "Get all Vehicles", description = "Retrieve a list of all vehicles available in the garage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful retrieval of vehicle list"),
            @ApiResponse(responseCode = "404", description = "No vehicles found")
    })
    public ResponseEntity<List<Vehicle>> getAll() {
        List<Vehicle> vehicles = vehicleQueryService.handle();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Search vehicles based on make, model, and year.
     *
     * @param brand The brand of the vehicle.
     * @param model The model of the vehicle.
     * @param year  The manufacturing year of the vehicle.
     * @return ResponseEntity containing the list of matching vehicles or a not found status.
     */
    @GetMapping("/search")
    @Operation(summary = "Search Vehicles", description = "Search for vehicles based on make, model, and year.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of search results"),
            @ApiResponse(responseCode = "404", description = "No vehicles found matching the search criteria")
    })
    public ResponseEntity<List<Vehicle>> search(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year) {
        List<Vehicle> vehicles = vehicleQueryService.handleSearch(brand, model, year);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get vehicle by ID.
     *
     * @param id The ID of the vehicle to retrieve.
     * @return ResponseEntity containing the vehicle or a not found status.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Vehicle by ID", description = "Retrieve vehicle details using the vehicle ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Vehicle> getById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleQueryService.handleById(id);
        return vehicle.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new vehicle.
     *
     * @param vehicle The vehicle details.
     * @return ResponseEntity containing the created vehicle or an error status.
     */
    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary="Create a vehicle", description = "Create a new vehicle with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle data")
    })
    public ResponseEntity<Vehicle> create(@RequestBody Vehicle vehicle) {
        Vehicle createdVehicle = vehicleCommandService.handle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
    }

    /**
     * Update an existing vehicle.
     *
     * @param id      The ID of the vehicle to update.
     * @param vehicle The updated vehicle details.
     * @return ResponseEntity containing the updated vehicle or an error status.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a Vehicle", description = "Update an existing vehicle with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle could not be found")
    })
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        Optional<Vehicle> updatedVehicle = vehicleCommandService.handleUpdate(id, vehicle);
        return updatedVehicle.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a vehicle by ID.
     *
     * @param id The ID of the vehicle to delete.
     * @return ResponseEntity indicating the result of the deletion operation.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Vehicle", description = "Delete an existing vehicle by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle could not be found")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = vehicleCommandService.handleDelete(id);
        return deleted ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /**
     * Mark a vehicle as favorite.
     *
     * @param id The ID of the vehicle to mark as favorite.
     * @return ResponseEntity containing the vehicle marked as favorite or a not found status.
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "Add Vehicle to Favorites", description = "Add a vehicle to the user's list of favorite vehicles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle added to favorites successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Vehicle> markAsFavorite(@PathVariable Long id) {
        Vehicle vehicle = addFavoriteCommandService.handle(id);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Remove a vehicle from favorites.
     *
     * @param id The ID of the vehicle to remove from favorites.
     * @return ResponseEntity indicating the result of the removal operation.
     */
    @DeleteMapping("/{id}/favorite")
    @Operation(summary = "Remove Vehicle from Favorites", description = "Remove a vehicle from the user's list of favorite vehicles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle removed from favorites successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Vehicle> removeFavorite(@PathVariable Long id) {
        Vehicle vehicle = removeFavoriteCommandService.handle(id);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Get all favorite vehicles.
     *
     * @return ResponseEntity containing the list of favorite vehicles or a not found status.
     */
    @GetMapping("/favorites")
    @Operation(summary = "Get Favorite Vehicles", description = "Retrieve a list of the user favorite vehicles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of favorite vehicles"),
            @ApiResponse(responseCode = "404", description = "No favorite vehicles found")
    })
    public ResponseEntity<List<Vehicle>> getFavorites() {
        List<Vehicle> favorites = vehicleQueryService.handleFavorites();
        return ResponseEntity.ok(favorites);
    }

}
