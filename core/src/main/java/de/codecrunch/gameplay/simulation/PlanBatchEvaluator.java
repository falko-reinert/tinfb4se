package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.TowerSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Runs canonical plans and keeps only compact aggregate data in memory. */
public final class PlanBatchEvaluator {
    private final PlanEnumerator enumerator = new PlanEnumerator();

    public BatchEvaluation evaluate(ScenarioSpec scenario, List<DeploymentPlan> plans,
                                    int determinismRepetitions) {
        if (scenario == null || plans == null) {
            throw new IllegalArgumentException("scenario and plans are required");
        }
        if (determinismRepetitions <= 0) {
            throw new IllegalArgumentException("determinismRepetitions must be positive");
        }

        ScenarioSpec reorderedScenario = reverseInputCollections(scenario);
        List<BatchPlanResult> planResults = new ArrayList<>();
        Map<String, BatchPlanResult> byKey = new LinkedHashMap<>();
        Map<String, MutableComposition> compositionCounts = new TreeMap<>();
        int victories = 0;
        int defeats = 0;
        int timeouts = 0;
        boolean deterministic = true;
        boolean incrementalParity = true;
        boolean collectionOrderIndependent = true;

        for (DeploymentPlan plan : plans) {
            SimulationResult primary = run(scenario, plan);
            if (primary.getResult() == SimulationStatus.INVALID_PLAN) {
                throw new IllegalArgumentException("Batch contains invalid plan: "
                        + enumerator.canonicalKey(plan));
            }
            String hash = primary.eventLogHash();
            for (int repetition = 1; repetition < determinismRepetitions; repetition++) {
                if (!hash.equals(run(scenario, plan).eventLogHash())) deterministic = false;
            }

            CombatSimulation incremental = new CombatSimulation(scenario, plan);
            SimulationState incrementalState = incremental.getState();
            while (!incrementalState.getStatus().isTerminal()) {
                incrementalState = incremental.advanceOneTick();
            }
            if (!hash.equals(incremental.getResult().eventLogHash())) {
                incrementalParity = false;
            }
            if (!hash.equals(run(reorderedScenario, plan).eventLogHash())) {
                collectionOrderIndependent = false;
            }

            if (primary.getResult() == SimulationStatus.VICTORY) victories++;
            else if (primary.getResult() == SimulationStatus.DEFEAT) defeats++;
            else if (primary.getResult() == SimulationStatus.TIMEOUT) timeouts++;

            String compositionKey = compositionKey(plan);
            MutableComposition composition = compositionCounts.get(compositionKey);
            if (composition == null) {
                composition = new MutableComposition(compositionKey);
                compositionCounts.put(compositionKey, composition);
            }
            composition.add(primary.getResult());

            BatchPlanResult compact = new BatchPlanResult(plan, primary.getResult(),
                    primary.getEndTick(), primary.getRemainingBaseIntegrity(), hash);
            String key = enumerator.canonicalKey(plan);
            planResults.add(compact);
            byKey.put(key, compact);
        }

        Map<String, CompositionSummary> compositions = new LinkedHashMap<>();
        for (MutableComposition mutable : compositionCounts.values()) {
            compositions.put(mutable.key, mutable.toImmutable());
        }
        return new BatchEvaluation(planResults, byKey, compositions,
                victories, defeats, timeouts, determinismRepetitions, deterministic,
                incrementalParity, collectionOrderIndependent);
    }

    public String compositionKey(DeploymentPlan plan) {
        List<String> types = new ArrayList<>();
        for (Deployment deployment : plan.getDeployments()) {
            types.add(deployment.getUnitTypeId());
        }
        Collections.sort(types);
        StringBuilder key = new StringBuilder();
        for (String type : types) {
            if (key.length() > 0) key.append('+');
            key.append(type);
        }
        return key.toString();
    }

    private SimulationResult run(ScenarioSpec scenario, DeploymentPlan plan) {
        return new CombatSimulation(scenario, plan).runToCompletion();
    }

    private ScenarioSpec reverseInputCollections(ScenarioSpec source) {
        List<UnitSpec> units = new ArrayList<>(source.getUnitSpecs());
        List<TowerSpec> towers = new ArrayList<>(source.getTowerSpecs());
        Collections.reverse(units);
        Collections.reverse(towers);
        return new ScenarioSpec(source.getScenarioId(), source.getRouteLengthUnits(),
                source.getBaseIntegrity(), source.getBudget(), source.getMaxDeployments(),
                source.getMaxSpawnTick(), source.getSpawnGridTicks(),
                source.getTimeLimitTicks(), units, towers);
    }

    private static final class MutableComposition {
        private final String key;
        private int total;
        private int victories;
        private int defeats;
        private int timeouts;

        private MutableComposition(String key) {
            this.key = key;
        }

        private void add(SimulationStatus result) {
            total++;
            if (result == SimulationStatus.VICTORY) victories++;
            else if (result == SimulationStatus.DEFEAT) defeats++;
            else if (result == SimulationStatus.TIMEOUT) timeouts++;
        }

        private CompositionSummary toImmutable() {
            return new CompositionSummary(key, total, victories, defeats, timeouts);
        }
    }
}
