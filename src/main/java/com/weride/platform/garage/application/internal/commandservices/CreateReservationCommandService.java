package com.weride.platform.garage.application.internal.commandservices;

import com.weride.platform.garage.domain.model.aggregates.Reservation;
import com.weride.platform.garage.infrastructure.persistence.jpa.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateReservationCommandService {
    private final ReservationRepository repository;

    public Reservation handle(Reservation reservation) {
        return repository.save(reservation);
    }
}
