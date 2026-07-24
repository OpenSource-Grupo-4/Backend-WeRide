package org.example.backendweride.platform.location.interfaces;

import org.example.backendweride.platform.location.domain.services.commandservices.LocationCommandService;
import org.example.backendweride.platform.location.domain.services.queryservices.LocationQueryService;
import org.example.backendweride.platform.location.interfaces.resources.LocationResource;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationControllerTest {

    private final LocationCommandService locationCommandService = mock(LocationCommandService.class);
    private final LocationQueryService locationQueryService = mock(LocationQueryService.class);

    private final LocationController controller = new LocationController(locationCommandService, locationQueryService);

    @Test
    void getAllLocation_returnsLocationResourceNotEntity() {
        when(locationQueryService.handle()).thenReturn(Optional.of(List.<LocationResource>of()));

        var response = controller.getAllLocation();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
