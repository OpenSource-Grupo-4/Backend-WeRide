package org.example.backendweride.platform.profile.domain.model.aggregates;

import org.example.backendweride.platform.profile.domain.model.commands.CreateProfileCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileTest {

    @Test
    void profileConstructorPersistsProvidedNameLastNameAndEmail() {
        var command = new CreateProfileCommand(1L, "Ada", "Lovelace", "ada@weride.com");
        var profile = new Profile(command);

        assertEquals("Ada", profile.getFirstName());
        assertEquals("Lovelace", profile.getLastName());
        assertEquals("ada@weride.com", profile.getEmail());
    }
}
