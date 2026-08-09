package de.codecrunch.gameplay.simulation;

/** Immutable final outcome for a resolved deployment. */
public final class UnitOutcomeResult {
    private final String deploymentId;
    private final String unitTypeId;
    private final SimulationUnitOutcome outcome;
    private final int tick;
    private final int remainingHealth;

    UnitOutcomeResult(String deploymentId, String unitTypeId,
                      SimulationUnitOutcome outcome, int tick, int remainingHealth) {
        this.deploymentId = deploymentId;
        this.unitTypeId = unitTypeId;
        this.outcome = outcome;
        this.tick = tick;
        this.remainingHealth = remainingHealth;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public SimulationUnitOutcome getOutcome() {
        return outcome;
    }

    public int getTick() {
        return tick;
    }

    public int getRemainingHealth() {
        return remainingHealth;
    }
}
