package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.plan.PlanValidationResult;
import de.codecrunch.gameplay.simulation.plan.PlanValidator;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Exhaustively enumerates the small, canonical deployment-plan search space. */
public final class PlanEnumerator {
    private static final Comparator<ScheduledUnit> SCHEDULE_ORDER =
            new Comparator<ScheduledUnit>() {
                @Override
                public int compare(ScheduledUnit left, ScheduledUnit right) {
                    int byTick = Integer.compare(left.spawnTick, right.spawnTick);
                    if (byTick != 0) return byTick;
                    return left.unitTypeId.compareTo(right.unitTypeId);
                }
            };

    /**
     * Enumerates every canonical valid plan with one to {@code maxDeployments} units.
     * At least one unit starts at tick zero; schedules that differ only by swapping equal
     * units are emitted once.
     */
    public List<DeploymentPlan> enumerateAllValidPlans(ScenarioSpec scenario) {
        requireScenario(scenario);
        TreeMap<String, DeploymentPlan> canonicalPlans = new TreeMap<>();
        List<String> unitTypes = new ArrayList<>();
        for (UnitSpec unitSpec : scenario.getUnitSpecs()) {
            unitTypes.add(unitSpec.getUnitTypeId());
        }
        for (int size = 1; size <= scenario.getMaxDeployments(); size++) {
            enumerateTypeSequences(scenario, unitTypes, size, new ArrayList<String>(),
                    canonicalPlans);
        }
        return immutableValues(canonicalPlans);
    }

    /**
     * Enumerates timings for an ordered composition whose first unit is anchored at tick
     * zero. Remaining units use every spawn-grid tick and are then canonicalized.
     */
    public List<DeploymentPlan> enumerateAnchoredComposition(ScenarioSpec scenario,
                                                              List<String> unitTypeIds) {
        requireScenario(scenario);
        if (unitTypeIds == null || unitTypeIds.isEmpty()) {
            throw new IllegalArgumentException("unitTypeIds are required");
        }
        if (unitTypeIds.size() > scenario.getMaxDeployments()) {
            return Collections.emptyList();
        }
        for (String unitTypeId : unitTypeIds) {
            if (scenario.getUnitSpec(unitTypeId) == null) {
                throw new IllegalArgumentException("Unknown unit type: " + unitTypeId);
            }
        }

        TreeMap<String, DeploymentPlan> canonicalPlans = new TreeMap<>();
        int[] ticks = new int[unitTypeIds.size()];
        enumerateTicks(scenario, unitTypeIds, ticks, 1, canonicalPlans);
        return immutableValues(canonicalPlans);
    }

    public String canonicalKey(DeploymentPlan plan) {
        if (plan == null) throw new IllegalArgumentException("plan is required");
        List<ScheduledUnit> schedule = new ArrayList<>();
        for (Deployment deployment : plan.getDeployments()) {
            schedule.add(new ScheduledUnit(deployment.getUnitTypeId(),
                    deployment.getSpawnTick()));
        }
        Collections.sort(schedule, SCHEDULE_ORDER);
        StringBuilder key = new StringBuilder();
        for (ScheduledUnit unit : schedule) {
            if (key.length() > 0) key.append(';');
            key.append(unit.unitTypeId).append('@').append(unit.spawnTick);
        }
        return key.toString();
    }

    private void enumerateTypeSequences(ScenarioSpec scenario, List<String> unitTypes,
                                        int targetSize, List<String> sequence,
                                        Map<String, DeploymentPlan> plans) {
        if (sequence.size() == targetSize) {
            int[] ticks = new int[targetSize];
            enumerateTicks(scenario, sequence, ticks, 1, plans);
            return;
        }
        for (String unitType : unitTypes) {
            sequence.add(unitType);
            enumerateTypeSequences(scenario, unitTypes, targetSize, sequence, plans);
            sequence.remove(sequence.size() - 1);
        }
    }

    private void enumerateTicks(ScenarioSpec scenario, List<String> unitTypes, int[] ticks,
                                int index, Map<String, DeploymentPlan> plans) {
        if (index == ticks.length) {
            addCanonicalIfValid(scenario, unitTypes, ticks, plans);
            return;
        }
        for (int tick = 0; tick <= scenario.getMaxSpawnTick();
             tick += scenario.getSpawnGridTicks()) {
            ticks[index] = tick;
            enumerateTicks(scenario, unitTypes, ticks, index + 1, plans);
        }
    }

    private void addCanonicalIfValid(ScenarioSpec scenario, List<String> unitTypes,
                                     int[] ticks, Map<String, DeploymentPlan> plans) {
        List<ScheduledUnit> schedule = new ArrayList<>();
        for (int index = 0; index < unitTypes.size(); index++) {
            schedule.add(new ScheduledUnit(unitTypes.get(index), ticks[index]));
        }
        Collections.sort(schedule, SCHEDULE_ORDER);

        List<Deployment> deployments = new ArrayList<>();
        for (int index = 0; index < schedule.size(); index++) {
            ScheduledUnit unit = schedule.get(index);
            int deploymentNumber = index + 1;
            String deploymentId = deploymentNumber < 10
                    ? "D0" + deploymentNumber : "D" + deploymentNumber;
            deployments.add(new Deployment(deploymentId,
                    unit.unitTypeId, unit.spawnTick));
        }
        PlanValidationResult validation = new PlanValidator().validateAndNormalize(
                scenario, new DeploymentPlan(deployments));
        if (!validation.isValid()) return;
        DeploymentPlan plan = validation.getNormalizedPlan();
        plans.put(canonicalKey(plan), plan);
    }

    private List<DeploymentPlan> immutableValues(Map<String, DeploymentPlan> plans) {
        return Collections.unmodifiableList(new ArrayList<>(plans.values()));
    }

    private void requireScenario(ScenarioSpec scenario) {
        if (scenario == null) throw new IllegalArgumentException("scenario is required");
    }

    private static final class ScheduledUnit {
        private final String unitTypeId;
        private final int spawnTick;

        private ScheduledUnit(String unitTypeId, int spawnTick) {
            this.unitTypeId = unitTypeId;
            this.spawnTick = spawnTick;
        }
    }
}
