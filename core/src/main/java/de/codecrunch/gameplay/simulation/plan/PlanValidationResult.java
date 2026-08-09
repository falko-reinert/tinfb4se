package de.codecrunch.gameplay.simulation.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Result of validation, including the canonical plan used by the simulation. */
public final class PlanValidationResult {
    private final DeploymentPlan normalizedPlan;
    private final List<PlanValidationError> errors;

    PlanValidationResult(DeploymentPlan normalizedPlan, List<PlanValidationError> errors) {
        this.normalizedPlan = normalizedPlan;
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public DeploymentPlan getNormalizedPlan() {
        return normalizedPlan;
    }

    public List<PlanValidationError> getErrors() {
        return errors;
    }
}
