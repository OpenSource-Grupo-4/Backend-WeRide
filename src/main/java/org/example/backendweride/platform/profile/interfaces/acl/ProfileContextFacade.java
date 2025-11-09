package org.example.backendweride.platform.profile.interfaces.acl;

import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.example.backendweride.platform.profile.domain.services.ProfileCommandService;
import org.springframework.stereotype.Service;

@Service
public class ProfileContextFacade {

    private final ProfileCommandService profileCommandService;

    public ProfileContextFacade(ProfileCommandService profileCommandService) {
        this.profileCommandService = profileCommandService;
    }

    /**
     * Crea un perfil vacío asociado a una cuenta de IAM.
     * @param accountId El ID de la cuenta de IAM
     * @return El ID del perfil creado
     */
    public Long createProfileForAccount(Long accountId) {
        var command = new CreateProfileCommand(accountId);
        var profile = profileCommandService.handle(command);
        return profile.getId();
    }
}