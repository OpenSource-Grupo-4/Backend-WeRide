package org.example.backendweride.platform.profile.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Profiles Controller
 *
 * @summary Handles HTTP requests related to user profiles.
 */
@RestController
@RequestMapping(value = "/api/v1/profiles", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Obtain and Update User Profiles")
public class ProfilesController {

    private final ProfileQueryService profileQueryService;
    private final ProfileCommandService profileCommandService;

    public ProfilesController(ProfileQueryService profileQueryService, ProfileCommandService profileCommandService) {
        this.profileQueryService = profileQueryService;
        this.profileCommandService = profileCommandService;
    }

    /**
     * Get profile by Account ID.
     * @param accountId The ID of the account associated with the profile.
     * @return ResponseEntity containing the profile resource or a not found status.
     */
    @GetMapping("by-account-id/{accountId}")
    @Operation (summary = "Get profile by Account ID", description = "Retrieve user profile details using the associated account ID.")
    @ApiResponses(value = {
            @ApiResponse (responseCode = "200", description = "Profile found"),
            @ApiResponse (responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ProfileResource> getProfileByAccountId(@PathVariable Long accountId) {
        var query = new GetProfileByAccountIdQuery(accountId);
        var profile = profileQueryService.handle(query);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var resource = ProfileResourceFromAggregateAssembler.toResource(profile.get());
        return ResponseEntity.ok(resource);
    }

    /**
     * Update profile by Profile ID.
     * @param profileId The ID of the profile to update.
     * @param resource The updated profile details.
     * @return ResponseEntity containing the updated profile resource or a not found status.
     */
    @PutMapping("/{profileId}")
    @Operation(summary = "Update profile by Profile ID", description = "Update user profile details using the profile ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
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