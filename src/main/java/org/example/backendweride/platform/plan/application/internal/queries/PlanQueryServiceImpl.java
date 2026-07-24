package org.example.backendweride.platform.plan.application.internal.queries;

import org.example.backendweride.platform.plan.domain.model.aggregates.Plan;
import org.example.backendweride.platform.plan.domain.queries.GetPlanById;
import org.example.backendweride.platform.plan.domain.services.queries.PlanQueryService;
import org.example.backendweride.platform.plan.infrastructure.persistence.jpa.PlanRepository;
import org.example.backendweride.platform.plan.interfaces.resources.PlanResource;
import org.example.backendweride.platform.plan.interfaces.transform.PlanResourceFromEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanQueryServiceImpl implements PlanQueryService {
    private final PlanRepository planRepository;
    public PlanQueryServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public Optional<Plan> handle(GetPlanById planId) {
        return this.planRepository.findById(planId.id());
    }

    @Override
    public Optional<List<PlanResource>> handle() {
        var result = this.planRepository.findAll().stream()
                .map(PlanResourceFromEntity::toPlanResource)
                .collect(Collectors.toList());
        return Optional.of(result);
    }
}
