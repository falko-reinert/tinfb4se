package de.codecrunch.gameplay.simulation;

/** Compact result of the exhaustive reference-scenario analysis. */
public final class ReferenceBatchReport {
    private final BatchEvaluation allPlans;
    private final int anchoredTimingPlans;
    private final int anchoredTimingVictories;
    private final int anchoredTimingDefeats;
    private final int anchoredTimingTimeouts;
    private final String robustWinningPlan;
    private final String adjacentWinningPlan;
    private final BalancingGateReport gates;

    ReferenceBatchReport(BatchEvaluation allPlans, int anchoredTimingPlans,
                         int anchoredTimingVictories, int anchoredTimingDefeats,
                         int anchoredTimingTimeouts, String robustWinningPlan,
                         String adjacentWinningPlan, BalancingGateReport gates) {
        this.allPlans = allPlans;
        this.anchoredTimingPlans = anchoredTimingPlans;
        this.anchoredTimingVictories = anchoredTimingVictories;
        this.anchoredTimingDefeats = anchoredTimingDefeats;
        this.anchoredTimingTimeouts = anchoredTimingTimeouts;
        this.robustWinningPlan = robustWinningPlan;
        this.adjacentWinningPlan = adjacentWinningPlan;
        this.gates = gates;
    }

    public BatchEvaluation getAllPlans() {
        return allPlans;
    }

    public int getAnchoredTimingPlans() {
        return anchoredTimingPlans;
    }

    public int getAnchoredTimingVictories() {
        return anchoredTimingVictories;
    }

    public int getAnchoredTimingDefeats() {
        return anchoredTimingDefeats;
    }

    public int getAnchoredTimingTimeouts() {
        return anchoredTimingTimeouts;
    }

    public String getRobustWinningPlan() {
        return robustWinningPlan;
    }

    public String getAdjacentWinningPlan() {
        return adjacentWinningPlan;
    }

    public BalancingGateReport getGates() {
        return gates;
    }
}
