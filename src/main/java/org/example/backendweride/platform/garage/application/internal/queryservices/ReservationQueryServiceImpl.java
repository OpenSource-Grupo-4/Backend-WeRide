package org.example.backendweride.platform.garage.application.internal.queryservices;

import org.example.backendweride.platform.garage.domain.model.aggregates.Reservation;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationQueryServiceImpl {
    private final ReservationRepository repository;

    public List<Reservation> handle() { return repository.findAll(); }
}
