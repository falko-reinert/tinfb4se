package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.plan.PlanValidationError;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable terminal result of a simulation run. */
public final class SimulationResult {
    private final String scenarioId;
    private final DeploymentPlan normalizedPlan;
    private final SimulationStatus result;
    private final int endTick;
    private final int remainingBaseIntegrity;
    private final int spentBudget;
    private final List<UnitOutcomeResult> unitOutcomes;
    private final List<SimulationEvent> events;
    private final List<PlanValidationError> validationErrors;

    SimulationResult(String scenarioId, DeploymentPlan normalizedPlan,
                     SimulationStatus result, int endTick, int remainingBaseIntegrity,
                     int spentBudget, List<UnitOutcomeResult> unitOutcomes,
                     List<SimulationEvent> events,
                     List<PlanValidationError> validationErrors) {
        this.scenarioId = scenarioId;
        this.normalizedPlan = normalizedPlan;
        this.result = result;
        this.endTick = endTick;
        this.remainingBaseIntegrity = remainingBaseIntegrity;
        this.spentBudget = spentBudget;
        this.unitOutcomes = immutableCopy(unitOutcomes);
        this.events = immutableCopy(events);
        this.validationErrors = immutableCopy(validationErrors);
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public DeploymentPlan getNormalizedPlan() {
        return normalizedPlan;
    }

    public SimulationStatus getResult() {
        return result;
    }

    public int getEndTick() {
        return endTick;
    }

    public int getRemainingBaseIntegrity() {
        return remainingBaseIntegrity;
    }

    public int getSpentBudget() {
        return spentBudget;
    }

    public List<UnitOutcomeResult> getUnitOutcomes() {
        return unitOutcomes;
    }

    public List<SimulationEvent> getEvents() {
        return events;
    }

    public List<PlanValidationError> getValidationErrors() {
        return validationErrors;
    }

    public String canonicalEventLog() {
        StringBuilder result = new StringBuilder();
        for (SimulationEvent event : events) {
            if (result.length() > 0) result.append('\n');
            result.append(event.toCanonicalString());
        }
        return result.toString();
    }

    public String eventLogHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalEventLog().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) hex.append(String.format("%02x", value & 0xff));
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", impossible);
        }
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return Collections.unmodifiableList(new ArrayList<>(source));
    }
}
