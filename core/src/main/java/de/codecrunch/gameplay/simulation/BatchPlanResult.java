package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;

/** Compact in-memory outcome retained by the batch evaluator. */
public final class BatchPlanResult {
    private final DeploymentPlan plan;
    private final SimulationStatus result;
    private final int endTick;
    private final int remainingBaseIntegrity;
    private final String eventLogHash;

    BatchPlanResult(DeploymentPlan plan, SimulationStatus result, int endTick,
                    int remainingBaseIntegrity, String eventLogHash) {
        this.plan = plan;
        this.result = result;
        this.endTick = endTick;
        this.remainingBaseIntegrity = remainingBaseIntegrity;
        this.eventLogHash = eventLogHash;
    }

    public DeploymentPlan getPlan() {
        return plan;
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

    public String getEventLogHash() {
        return eventLogHash;
    }
}
