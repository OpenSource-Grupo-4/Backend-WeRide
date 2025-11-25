package org.example.backendweride.platform.garage.interfaces.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.garage.application.internal.queryservices.UserQueryServiceImpl;
import org.example.backendweride.platform.garage.domain.model.aggregates.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * UserController handles HTTP requests related to users.
 *
 * @summary This controller provides endpoints for retrieving user information.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/users", produces = APPLICATION_JSON_VALUE)
@Tag(name = "User", description = "Users Manager")
public class UserController {

    private final UserQueryServiceImpl userQueryService;

    /**
     * Get all users.
     * @return List of all users.
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponses(value = {
            @ApiResponse (responseCode = "201", description = "Founded Users"),
            @ApiResponse (responseCode = "404", description = "Users not Found")
    })
    public List<User> getAllUsers() { return userQueryService.handle(); }
}
