package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.plan.PlanValidationResult;
import de.codecrunch.gameplay.simulation.plan.PlanValidator;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.TargetRule;
import de.codecrunch.gameplay.simulation.spec.TowerSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Renderer-free deterministic combat rules.
 *
 * <p>Every call to {@link #advanceOneTick()} applies this fixed order: spawn by
 * deployment ID, move and emit range entries by deployment/tower ID, resolve breaches,
 * fire towers by tower ID, remove defeated units by deployment ID, then resolve defeat or
 * timeout. A breach therefore wins over a shot in the same tick. All route values are
 * integer fixed-point units; no elapsed-time float enters this class.</p>
 */
public final class CombatSimulation {
    private final ScenarioSpec scenario;
    private final DeploymentPlan normalizedPlan;
    private final PlanValidationResult validation;
    private final List<Deployment> deployments;
    private final TreeMap<String, UnitRuntime> activeUnits = new TreeMap<>();
    private final TreeMap<String, TowerRuntime> towers = new TreeMap<>();
    private final LinkedHashMap<String, UnitOutcomeResult> unitOutcomes = new LinkedHashMap<>();
    private final List<SimulationEvent> events = new ArrayList<>();
    private int nextDeploymentIndex;
    private int currentTick;
    private int baseIntegrity;
    private int finishedTick = -1;
    private int spentBudget;
    private SimulationStatus status;

    public CombatSimulation(ScenarioSpec scenario, DeploymentPlan plan) {
        if (scenario == null) throw new IllegalArgumentException("scenario is required");
        this.scenario = scenario;
        this.validation = new PlanValidator().validateAndNormalize(scenario, plan);
        this.normalizedPlan = validation.getNormalizedPlan();
        this.deployments = normalizedPlan.getDeployments();
        this.baseIntegrity = scenario.getBaseIntegrity();

        for (TowerSpec towerSpec : scenario.getTowerSpecs()) {
            towers.put(towerSpec.getTowerId(), new TowerRuntime(towerSpec));
        }
        if (!validation.isValid()) {
            status = SimulationStatus.INVALID_PLAN;
            finishedTick = 0;
            events.add(SimulationEvent.finished(0, status));
            return;
        }
        for (Deployment deployment : deployments) {
            spentBudget += scenario.getUnitSpec(deployment.getUnitTypeId()).getCost();
        }
        status = SimulationStatus.RUNNING;
    }

    /** Processes exactly one tick unless the simulation is already terminal. */
    public SimulationState advanceOneTick() {
        if (status.isTerminal()) return snapshot();
        processOneTick();
        return snapshot();
    }

    private void processOneTick() {
        final int tick = currentTick;

        spawnDueUnits(tick);
        moveUnitsAndRecordRangeEntries(tick);
        resolveBreaches(tick);
        if (status == SimulationStatus.RUNNING) fireReadyTowers(tick);
        if (status == SimulationStatus.RUNNING) removeDefeatedUnits(tick);
        if (status == SimulationStatus.RUNNING) resolveTerminalState(tick);

        currentTick++;
    }

    public SimulationState advanceTicks(int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must not be negative");
        for (int count = 0; count < ticks && status == SimulationStatus.RUNNING; count++) {
            processOneTick();
        }
        return snapshot();
    }

    public SimulationResult runToCompletion() {
        while (status == SimulationStatus.RUNNING) processOneTick();
        return result();
    }

    public SimulationState getState() {
        return snapshot();
    }

    public SimulationResult getResult() {
        if (!status.isTerminal()) {
            throw new IllegalStateException("Simulation is still running");
        }
        return result();
    }

    private void spawnDueUnits(int tick) {
        int end = nextDeploymentIndex;
        while (end < deployments.size() && deployments.get(end).getSpawnTick() == tick) end++;
        List<Deployment> due = new ArrayList<>(deployments.subList(nextDeploymentIndex, end));
        Collections.sort(due, (left, right) -> left.getDeploymentId()
                .compareTo(right.getDeploymentId()));
        for (Deployment deployment : due) {
            UnitSpec unitSpec = scenario.getUnitSpec(deployment.getUnitTypeId());
            UnitRuntime runtime = new UnitRuntime(deployment, unitSpec);
            activeUnits.put(deployment.getDeploymentId(), runtime);
            events.add(SimulationEvent.unit(tick, SimulationEventType.UNIT_SPAWNED,
                    deployment.getDeploymentId(), 0L));
        }
        nextDeploymentIndex = end;
    }

    private void moveUnitsAndRecordRangeEntries(int tick) {
        for (UnitRuntime unit : activeUnits.values()) {
            if (unit.health <= 0) continue;
            long previousProgress = unit.routeProgressUnits;
            long remaining = scenario.getRouteLengthUnits() - previousProgress;
            unit.routeProgressUnits = unit.spec.getSpeedUnitsPerTick() >= remaining
                    ? scenario.getRouteLengthUnits()
                    : previousProgress + unit.spec.getSpeedUnitsPerTick();

            for (TowerRuntime tower : towers.values()) {
                boolean wasInside = unit.hasMoved && inRange(tower.spec, previousProgress);
                boolean isInside = inRange(tower.spec, unit.routeProgressUnits);
                if (!wasInside && isInside) {
                    events.add(SimulationEvent.rangeEntered(tick,
                            unit.deployment.getDeploymentId(), tower.spec.getTowerId(),
                            unit.routeProgressUnits));
                }
            }
            unit.hasMoved = true;
        }
    }

    private void resolveBreaches(int tick) {
        List<String> breachedIds = new ArrayList<>();
        for (Map.Entry<String, UnitRuntime> entry : activeUnits.entrySet()) {
            if (entry.getValue().health > 0
                    && entry.getValue().routeProgressUnits >= scenario.getRouteLengthUnits()) {
                breachedIds.add(entry.getKey());
            }
        }
        for (String deploymentId : breachedIds) {
            UnitRuntime unit = activeUnits.remove(deploymentId);
            events.add(SimulationEvent.unit(tick, SimulationEventType.UNIT_BREACHED,
                    deploymentId, unit.routeProgressUnits));
            baseIntegrity = Math.max(0, baseIntegrity - unit.spec.getBaseDamage());
            events.add(SimulationEvent.baseDamaged(tick, deploymentId,
                    unit.spec.getBaseDamage(), baseIntegrity, unit.routeProgressUnits));
            unitOutcomes.put(deploymentId, new UnitOutcomeResult(deploymentId,
                    unit.spec.getUnitTypeId(), SimulationUnitOutcome.BREACHED, tick, unit.health));
            if (baseIntegrity == 0) {
                finish(tick, SimulationStatus.VICTORY);
                return;
            }
        }
    }

    private void fireReadyTowers(int tick) {
        for (TowerRuntime tower : towers.values()) {
            if (tick < tower.nextReadyTick) continue;
            UnitRuntime target = selectTarget(tower.spec);
            if (target == null) continue;

            events.add(SimulationEvent.shot(tick, SimulationEventType.TOWER_FIRED,
                    target.deployment.getDeploymentId(), tower.spec.getTowerId(),
                    tower.spec.getDamage(), -1, target.routeProgressUnits));
            target.health = Math.max(0, target.health - tower.spec.getDamage());
            events.add(SimulationEvent.shot(tick, SimulationEventType.UNIT_DAMAGED,
                    target.deployment.getDeploymentId(), tower.spec.getTowerId(),
                    tower.spec.getDamage(), target.health, target.routeProgressUnits));
            tower.nextReadyTick = tower.spec.getCooldownTicks() > Integer.MAX_VALUE - tick
                    ? Integer.MAX_VALUE : tick + tower.spec.getCooldownTicks();
        }
    }

    private UnitRuntime selectTarget(TowerSpec tower) {
        if (tower.getTargetRule() != TargetRule.FURTHEST_PROGRESS) {
            throw new IllegalStateException("Unsupported target rule: " + tower.getTargetRule());
        }
        UnitRuntime selected = null;
        for (UnitRuntime candidate : activeUnits.values()) {
            if (candidate.health <= 0 || !inRange(tower, candidate.routeProgressUnits)) continue;
            if (selected == null
                    || candidate.routeProgressUnits > selected.routeProgressUnits
                    || candidate.routeProgressUnits == selected.routeProgressUnits
                    && candidate.deployment.getDeploymentId()
                    .compareTo(selected.deployment.getDeploymentId()) < 0) {
                selected = candidate;
            }
        }
        return selected;
    }

    private boolean inRange(TowerSpec tower, long routeProgressUnits) {
        long lower = Math.max(0L, tower.getRoutePositionUnits() - tower.getRangeUnits());
        long upper = Math.min(scenario.getRouteLengthUnits(),
                safeAdd(tower.getRoutePositionUnits(), tower.getRangeUnits()));
        return routeProgressUnits >= lower && routeProgressUnits <= upper;
    }

    private long safeAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    private void removeDefeatedUnits(int tick) {
        List<String> defeatedIds = new ArrayList<>();
        for (Map.Entry<String, UnitRuntime> entry : activeUnits.entrySet()) {
            if (entry.getValue().health <= 0) defeatedIds.add(entry.getKey());
        }
        for (String deploymentId : defeatedIds) {
            UnitRuntime unit = activeUnits.remove(deploymentId);
            events.add(SimulationEvent.unit(tick, SimulationEventType.UNIT_DEFEATED,
                    deploymentId, unit.routeProgressUnits));
            unitOutcomes.put(deploymentId, new UnitOutcomeResult(deploymentId,
                    unit.spec.getUnitTypeId(), SimulationUnitOutcome.DEFEATED, tick, 0));
        }
    }

    private void resolveTerminalState(int tick) {
        if (activeUnits.isEmpty() && nextDeploymentIndex >= deployments.size()) {
            finish(tick, SimulationStatus.DEFEAT);
        } else if (tick + 1 >= scenario.getTimeLimitTicks()) {
            finish(tick, SimulationStatus.TIMEOUT);
        }
    }

    private void finish(int tick, SimulationStatus terminalStatus) {
        if (status.isTerminal()) return;
        status = terminalStatus;
        finishedTick = tick;
        events.add(SimulationEvent.finished(tick, terminalStatus));
    }

    private SimulationState snapshot() {
        List<ActiveUnitState> unitStates = new ArrayList<>();
        for (UnitRuntime unit : activeUnits.values()) {
            unitStates.add(new ActiveUnitState(unit.deployment.getDeploymentId(),
                    unit.spec.getUnitTypeId(), unit.health, unit.routeProgressUnits));
        }
        List<TowerState> towerStates = new ArrayList<>();
        for (TowerRuntime tower : towers.values()) {
            towerStates.add(new TowerState(tower.spec.getTowerId(), tower.nextReadyTick));
        }
        return new SimulationState(currentTick, baseIntegrity, status,
                unitStates, towerStates, events);
    }

    private SimulationResult result() {
        return new SimulationResult(scenario.getScenarioId(), normalizedPlan, status,
                finishedTick, baseIntegrity, spentBudget,
                new ArrayList<>(unitOutcomes.values()), events, validation.getErrors());
    }

    private static final class UnitRuntime {
        private final Deployment deployment;
        private final UnitSpec spec;
        private int health;
        private long routeProgressUnits;
        private boolean hasMoved;

        private UnitRuntime(Deployment deployment, UnitSpec spec) {
            this.deployment = deployment;
            this.spec = spec;
            this.health = spec.getMaxHealth();
        }
    }

    private static final class TowerRuntime {
        private final TowerSpec spec;
        private int nextReadyTick;

        private TowerRuntime(TowerSpec spec) {
            this.spec = spec;
        }
    }
}
