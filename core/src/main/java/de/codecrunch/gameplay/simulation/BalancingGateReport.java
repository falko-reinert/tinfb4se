package de.codecrunch.gameplay.simulation;

/** Explicit result for the seven balancing gates in GameplayDiscovery.md. */
public final class BalancingGateReport {
    private final boolean hasWinningAndLosingPlan;
    private final boolean bulwarkRunnerRunnerDependsOnTiming;
    private final boolean threeRunnersNeverWin;
    private final boolean hasMultipleWinningPlans;
    private final boolean hasAdjacentRobustWinningPlan;
    private final boolean eventLogsRepeatOneHundredTimes;
    private final boolean independentOfExecutionAndCollectionOrder;

    BalancingGateReport(boolean hasWinningAndLosingPlan,
                        boolean bulwarkRunnerRunnerDependsOnTiming,
                        boolean threeRunnersNeverWin,
                        boolean hasMultipleWinningPlans,
                        boolean hasAdjacentRobustWinningPlan,
                        boolean eventLogsRepeatOneHundredTimes,
                        boolean independentOfExecutionAndCollectionOrder) {
        this.hasWinningAndLosingPlan = hasWinningAndLosingPlan;
        this.bulwarkRunnerRunnerDependsOnTiming = bulwarkRunnerRunnerDependsOnTiming;
        this.threeRunnersNeverWin = threeRunnersNeverWin;
        this.hasMultipleWinningPlans = hasMultipleWinningPlans;
        this.hasAdjacentRobustWinningPlan = hasAdjacentRobustWinningPlan;
        this.eventLogsRepeatOneHundredTimes = eventLogsRepeatOneHundredTimes;
        this.independentOfExecutionAndCollectionOrder =
                independentOfExecutionAndCollectionOrder;
    }

    public boolean hasWinningAndLosingPlan() {
        return hasWinningAndLosingPlan;
    }

    public boolean bulwarkRunnerRunnerDependsOnTiming() {
        return bulwarkRunnerRunnerDependsOnTiming;
    }

    public boolean threeRunnersNeverWin() {
        return threeRunnersNeverWin;
    }

    public boolean hasMultipleWinningPlans() {
        return hasMultipleWinningPlans;
    }

    public boolean hasAdjacentRobustWinningPlan() {
        return hasAdjacentRobustWinningPlan;
    }

    public boolean eventLogsRepeatOneHundredTimes() {
        return eventLogsRepeatOneHundredTimes;
    }

    public boolean independentOfExecutionAndCollectionOrder() {
        return independentOfExecutionAndCollectionOrder;
    }

    public boolean allPassed() {
        return hasWinningAndLosingPlan
                && bulwarkRunnerRunnerDependsOnTiming
                && threeRunnersNeverWin
                && hasMultipleWinningPlans
                && hasAdjacentRobustWinningPlan
                && eventLogsRepeatOneHundredTimes
                && independentOfExecutionAndCollectionOrder;
    }
}
