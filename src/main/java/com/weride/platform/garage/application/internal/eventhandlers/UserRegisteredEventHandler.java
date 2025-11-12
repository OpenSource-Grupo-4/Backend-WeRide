package com.weride.platform.garage.application.internal.eventhandlers;

import com.weride.platform.garage.domain.model.events.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRegisteredEventHandler {
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("User registered: {} ({})", event.getUserId(), event.getEmail());
    }
}
