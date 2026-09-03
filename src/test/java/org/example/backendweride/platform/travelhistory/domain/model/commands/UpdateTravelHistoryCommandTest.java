package org.example.backendweride.platform.travelhistory.domain.model.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateTravelHistoryCommandTest {
    @Test
    void updateTravelHistoryCommandExposesRecordIdNotUserId() {
        var command = new UpdateTravelHistoryCommand(5L, 42L, "loc", "vehicle", "img", "10m", "2km");
        assertEquals(5L, command.id());
    }
}
