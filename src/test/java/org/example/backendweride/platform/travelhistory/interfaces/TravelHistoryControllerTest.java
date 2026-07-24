package org.example.backendweride.platform.travelhistory.interfaces;

import org.example.backendweride.platform.travelhistory.domain.model.queries.GetAllTravelsHistory;
import org.example.backendweride.platform.travelhistory.domain.model.queries.GetTravelsHistoryById;
import org.example.backendweride.platform.travelhistory.domain.services.commandservices.TravelHistoryCommandService;
import org.example.backendweride.platform.travelhistory.domain.services.queryservices.TravelHistoryQueryService;
import org.example.backendweride.platform.travelhistory.interfaces.resources.TravelHistoryResource;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TravelHistoryControllerTest {

    private final TravelHistoryCommandService travelHistoryCommandService = mock(TravelHistoryCommandService.class);
    private final TravelHistoryQueryService travelHistoryQueryService = mock(TravelHistoryQueryService.class);

    private final TravelHistoryController controller = new TravelHistoryController(travelHistoryCommandService, travelHistoryQueryService);

    @Test
    void getAllTravelHistories_returnsTravelHistoryResourceNotEntity() {
        when(travelHistoryQueryService.handle(any(GetAllTravelsHistory.class)))
                .thenReturn(Optional.of(List.<TravelHistoryResource>of()));

        var response = controller.getAllTravelHistories();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getTravelHistoryById_returnsTravelHistoryResourceNotEntity() {
        when(travelHistoryQueryService.handle(any(GetTravelsHistoryById.class)))
                .thenReturn(Optional.of(List.<TravelHistoryResource>of()));

        var response = controller.getTravelHistoryById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
