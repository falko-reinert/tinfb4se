package de.codecrunch.gameplay.planning;

import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.plan.PlanValidationError;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable state consumed by the LibGDX planning HUD. */
public final class PlanningSnapshot {
    private final ScenarioSpec scenario;
    private final List<PlanningSlotView> slots;
    private final int spentBudget;
    private final List<PlanValidationError> validationErrors;
    private final boolean submitted;
    private final DeploymentPlan submittedPlan;

    PlanningSnapshot(ScenarioSpec scenario, List<PlanningSlotView> slots, int spentBudget,
                     List<PlanValidationError> validationErrors, boolean submitted,
                     DeploymentPlan submittedPlan) {
        this.scenario = scenario;
        this.slots = Collections.unmodifiableList(new ArrayList<>(slots));
        this.spentBudget = spentBudget;
        this.validationErrors = Collections.unmodifiableList(
                new ArrayList<>(validationErrors));
        this.submitted = submitted;
        this.submittedPlan = submittedPlan;
    }

    public ScenarioSpec getScenario() {
        return scenario;
    }

    public List<PlanningSlotView> getSlots() {
        return slots;
    }

    public int getSpentBudget() {
        return spentBudget;
    }

    public int getRemainingBudget() {
        return scenario.getBudget() - spentBudget;
    }

    public List<PlanValidationError> getValidationErrors() {
        return validationErrors;
    }

    public boolean isValid() {
        return validationErrors.isEmpty();
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public DeploymentPlan getSubmittedPlan() {
        return submittedPlan;
    }
}
