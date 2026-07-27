package org.example.backendweride.platform.plan.domain.services.commands;

import org.example.backendweride.platform.plan.domain.commands.CreatePlanCommand;
import org.example.backendweride.platform.plan.domain.model.aggregates.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanCommandService {
    Optional<Plan> handle(CreatePlanCommand command);
    boolean handle(Long id);
    Optional<Plan> handle(Long id, CreatePlanCommand command);

}
