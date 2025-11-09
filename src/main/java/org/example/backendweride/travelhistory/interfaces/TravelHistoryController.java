package org.example.backendweride.travelhistory.interfaces;

import org.example.backendweride.travelhistory.domain.model.aggregates.TravelHistory;
import org.example.backendweride.travelhistory.domain.model.queries.GetTravelsHistoryById;
import org.example.backendweride.travelhistory.domain.services.commandservices.TravelHistoryCommandService;
import org.example.backendweride.travelhistory.domain.services.queryservices.TravelHistoryQueryService;
import org.example.backendweride.travelhistory.interfaces.resources.CreateTravelHistoryResource;
import org.example.backendweride.travelhistory.interfaces.resources.TravelHistoryResource;
import org.example.backendweride.travelhistory.interfaces.transform.CreateTravelHistoryCommandFromResourceAssembler;
import org.example.backendweride.travelhistory.interfaces.transform.TravelHistoryResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;


@RestController
@RequestMapping(value = "/api/travel-history")
public class TravelHistoryController {
    private final TravelHistoryCommandService travelHistoryCommandService;
    private final TravelHistoryQueryService travelHistoryQueryService;

    public TravelHistoryController(
            TravelHistoryCommandService travelHistoryCommandService,
            TravelHistoryQueryService travelHistoryQueryService
    ) {
        this.travelHistoryCommandService = travelHistoryCommandService;
       this.travelHistoryQueryService = travelHistoryQueryService;
    }

    @PostMapping
    public ResponseEntity<TravelHistoryResource> createTravelHistory(@RequestBody CreateTravelHistoryResource travelHistoryResource) {
        var result = travelHistoryCommandService.handle(CreateTravelHistoryCommandFromResourceAssembler.toCommandFronResource(travelHistoryResource));
        return result.map(travelHistory -> new ResponseEntity<>(
                TravelHistoryResourceFromEntityAssembler.toTravelHistoryFromEntity(travelHistory), CREATED
        )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("{userId}")
    public ResponseEntity<List<TravelHistory>> getTravelHistoryById(@PathVariable Long userId) {
        var result = travelHistoryQueryService.handle(new GetTravelsHistoryById(userId));
        return result.map(response -> new ResponseEntity<>(response, HttpStatus.FOUND))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
