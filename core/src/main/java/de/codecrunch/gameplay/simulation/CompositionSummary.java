package de.codecrunch.gameplay.simulation;

/** Aggregated result counts for one sorted unit composition. */
public final class CompositionSummary {
    private final String compositionKey;
    private final int totalPlans;
    private final int victories;
    private final int defeats;
    private final int timeouts;

    CompositionSummary(String compositionKey, int totalPlans, int victories,
                       int defeats, int timeouts) {
        this.compositionKey = compositionKey;
        this.totalPlans = totalPlans;
        this.victories = victories;
        this.defeats = defeats;
        this.timeouts = timeouts;
    }

    public String getCompositionKey() {
        return compositionKey;
    }

    public int getTotalPlans() {
        return totalPlans;
    }

    public int getVictories() {
        return victories;
    }

    public int getDefeats() {
        return defeats;
    }

    public int getTimeouts() {
        return timeouts;
    }

    public int getNonVictories() {
        return defeats + timeouts;
    }
}
