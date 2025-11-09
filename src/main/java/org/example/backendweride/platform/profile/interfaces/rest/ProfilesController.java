package org.example.backendweride.platform.profile.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.backendweride.platform.profile.domain.model.queries.GetProfileByAccountIdQuery;
import org.example.backendweride.platform.profile.domain.services.ProfileCommandService;
import org.example.backendweride.platform.profile.domain.services.ProfileQueryService;
import org.example.backendweride.platform.profile.interfaces.rest.resources.ProfileResource;
import org.example.backendweride.platform.profile.interfaces.rest.resources.UpdateProfileResource;
import org.example.backendweride.platform.profile.interfaces.rest.transform.ProfileResourceFromAggregateAssembler;
import org.example.backendweride.platform.profile.interfaces.rest.transform.UpdateProfileCommandFromResourceAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@Tag(name = "Profiles", description = "Endpoints para gestionar Perfiles de Usuario")
public class ProfilesController {

    private final ProfileQueryService profileQueryService;
    private final ProfileCommandService profileCommandService;

    public ProfilesController(ProfileQueryService profileQueryService, ProfileCommandService profileCommandService) {
        this.profileQueryService = profileQueryService;
        this.profileCommandService = profileCommandService;
    }

    @GetMapping("by-account-id/{accountId}")
    public ResponseEntity<ProfileResource> getProfileByAccountId(@PathVariable Long accountId) {
        var query = new GetProfileByAccountIdQuery(accountId);
        var profile = profileQueryService.handle(query);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var resource = ProfileResourceFromAggregateAssembler.toResource(profile.get());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{profileId}")
    public ResponseEntity<ProfileResource> updateProfile(@PathVariable Long profileId, @RequestBody UpdateProfileResource resource) {
        var command = UpdateProfileCommandFromResourceAssembler.toCommand(profileId, resource);
        var profile = profileCommandService.handle(command);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var res = ProfileResourceFromAggregateAssembler.toResource(profile.get());
        return ResponseEntity.ok(res);
    }
}