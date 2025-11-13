package org.example.backendweride.platform.garage.application.internal.commandservices;

import org.example.backendweride.platform.garage.domain.model.aggregates.Reservation;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories.ReservationRepository;
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
