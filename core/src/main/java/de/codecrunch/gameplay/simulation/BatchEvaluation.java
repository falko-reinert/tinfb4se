package de.codecrunch.gameplay.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Immutable aggregate produced without export or renderer integration. */
public final class BatchEvaluation {
    private final List<BatchPlanResult> plans;
    private final Map<String, BatchPlanResult> plansByKey;
    private final Map<String, CompositionSummary> compositions;
    private final int victories;
    private final int defeats;
    private final int timeouts;
    private final int determinismRepetitions;
    private final boolean deterministicEventLogs;
    private final boolean incrementalExecutionParity;
    private final boolean inputCollectionOrderIndependent;

    BatchEvaluation(List<BatchPlanResult> plans,
                    Map<String, BatchPlanResult> plansByKey,
                    Map<String, CompositionSummary> compositions,
                    int victories, int defeats, int timeouts,
                    int determinismRepetitions, boolean deterministicEventLogs,
                    boolean incrementalExecutionParity,
                    boolean inputCollectionOrderIndependent) {
        this.plans = Collections.unmodifiableList(new ArrayList<>(plans));
        this.plansByKey = Collections.unmodifiableMap(new LinkedHashMap<>(plansByKey));
        this.compositions = Collections.unmodifiableMap(new LinkedHashMap<>(compositions));
        this.victories = victories;
        this.defeats = defeats;
        this.timeouts = timeouts;
        this.determinismRepetitions = determinismRepetitions;
        this.deterministicEventLogs = deterministicEventLogs;
        this.incrementalExecutionParity = incrementalExecutionParity;
        this.inputCollectionOrderIndependent = inputCollectionOrderIndependent;
    }

    public List<BatchPlanResult> getPlans() {
        return plans;
    }

    public BatchPlanResult getPlan(String canonicalKey) {
        return plansByKey.get(canonicalKey);
    }

    public Map<String, CompositionSummary> getCompositions() {
        return compositions;
    }

    public CompositionSummary getComposition(String compositionKey) {
        return compositions.get(compositionKey);
    }

    public int getTotalPlans() {
        return plans.size();
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

    public int getDeterminismRepetitions() {
        return determinismRepetitions;
    }

    public boolean hasDeterministicEventLogs() {
        return deterministicEventLogs;
    }

    public boolean hasIncrementalExecutionParity() {
        return incrementalExecutionParity;
    }

    public boolean isInputCollectionOrderIndependent() {
        return inputCollectionOrderIndependent;
    }
}
