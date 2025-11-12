package com.weride.platform.garage.application.internal.eventhandlers;

import com.weride.platform.garage.domain.model.events.VehicleFavoritedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VehicleFavoritedEventHandler {
    @EventListener
    public void onVehicleFavorited(VehicleFavoritedEvent event) {
        log.info("Vehicle favorited: {} by user {}", event.getVehicleId(), event.getUserId());
    }
}
