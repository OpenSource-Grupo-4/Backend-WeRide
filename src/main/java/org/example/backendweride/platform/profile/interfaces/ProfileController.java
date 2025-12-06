package org.example.backendweride.platform.profile.interfaces;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.profile.domain.model.aggregates.Profile;
import org.example.backendweride.platform.profile.domain.services.commands.ProfileCommandService;
import org.example.backendweride.platform.profile.domain.services.queries.ProfileQueryService;
import org.example.backendweride.platform.profile.infrastructure.persistence.jpa.ProfileRepository;
import org.example.backendweride.platform.profile.interfaces.resources.CreateProfileCommandResource;
import org.example.backendweride.platform.profile.interfaces.resources.ProfileResource;
import org.example.backendweride.platform.profile.interfaces.transform.CreateProfileCommandFromResourceAssembler;
import org.example.backendweride.platform.profile.interfaces.transform.ProfileResourceFromEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/profiles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "profiles")
public class ProfileController {
    private final ProfileCommandService  profileCommandService;
    private final ProfileQueryService profileQueryService;

    public ProfileController(ProfileCommandService profileCommandService, ProfileQueryService profileQueryService) {
        this.profileCommandService = profileCommandService;
        this.profileQueryService =profileQueryService;
    }

    @PostMapping
    public ResponseEntity<ProfileResource> createProfile(@RequestBody CreateProfileCommandResource profileResource) {
        var result = this.profileCommandService.handle(CreateProfileCommandFromResourceAssembler.toCommandFromResource(profileResource));

        return result.map(response -> new ResponseEntity<>(
                ProfileResourceFromEntity.tpProfileResourceFromEntity(response), CREATED
        )).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        var result = this.profileQueryService.handle(id);
        return result.map(response -> new ResponseEntity<>(
                response, CREATED
        )).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

}
