package org.example.backendweride.platform.plan.interfaces;

import org.example.backendweride.platform.plan.domain.services.commands.PlanCommandService;
import org.example.backendweride.platform.plan.domain.services.queries.PlanQueryService;
import org.example.backendweride.platform.plan.interfaces.resources.PlanResource;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlanControllerTest {

    private final PlanCommandService planCommandService = mock(PlanCommandService.class);
    private final PlanQueryService planQueryService = mock(PlanQueryService.class);

    private final PlanController controller = new PlanController(planCommandService, planQueryService);

    @Test
    void findAllPlans_returnsPlanResourceNotEntity() {
        when(planQueryService.handle()).thenReturn(Optional.of(List.<PlanResource>of()));

        var response = controller.findAllPlans();

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
