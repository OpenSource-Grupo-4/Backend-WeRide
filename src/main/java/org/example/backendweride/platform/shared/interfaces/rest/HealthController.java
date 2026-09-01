package org.example.backendweride.platform.shared.interfaces.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Public liveness endpoint used by the hosting platform.
 */
@RestController
public class HealthController {

    @GetMapping(value = "/health", produces = APPLICATION_JSON_VALUE)
    public HealthResponse health() {
        return new HealthResponse("UP", "Backend-WeRide");
    }

    public record HealthResponse(String status, String service) {}
}
