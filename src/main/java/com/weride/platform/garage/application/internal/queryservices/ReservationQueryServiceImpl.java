package com.weride.platform.garage.application.internal.queryservices;

import com.weride.platform.garage.domain.model.aggregates.Reservation;
import com.weride.platform.garage.infrastructure.persistence.jpa.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationQueryServiceImpl {
    private final ReservationRepository repository;

    public List<Reservation> handle() { return repository.findAll(); }
}
