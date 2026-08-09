package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.TargetRule;
import de.codecrunch.gameplay.simulation.spec.TowerSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CombatSimulationRulesTest {
    @Test
    public void bothInclusiveRangeBoundariesCanBeTargeted() {
        SimulationResult lower = run(singleUnitScenario(3L, 10, 1,
                tower("T", 5L, 2L, 1, 1)));
        SimulationResult upper = run(singleUnitScenario(7L, 10, 1,
                tower("T", 5L, 2L, 1, 1)));

        assertEquals(1, events(lower, SimulationEventType.TOWER_FIRED).size());
        assertEquals(3L, events(lower, SimulationEventType.TOWER_FIRED)
                .get(0).getRouteProgressUnits());
        assertEquals(1, events(upper, SimulationEventType.TOWER_FIRED).size());
        assertEquals(7L, events(upper, SimulationEventType.TOWER_FIRED)
                .get(0).getRouteProgressUnits());
    }

    @Test
    public void unitThatSkipsEntireRangeIsNeverInside() {
        SimulationResult result = run(singleUnitScenario(8L, 10, 1,
                tower("T", 5L, 2L, 1, 1)));

        assertTrue(events(result, SimulationEventType.TOWER_FIRED).isEmpty());
        assertTrue(events(result, SimulationEventType.UNIT_ENTERED_TOWER_RANGE).isEmpty());
    }

    @Test
    public void oneRouteUnitOutsideEitherBoundaryIsNotTargeted() {
        SimulationResult below = run(singleUnitScenario(2L, 10, 1,
                tower("T", 5L, 2L, 1, 1)));
        SimulationResult above = run(singleUnitScenario(8L, 10, 1,
                tower("T", 5L, 2L, 1, 1)));

        assertTrue(events(below, SimulationEventType.TOWER_FIRED).isEmpty());
        assertTrue(events(above, SimulationEventType.TOWER_FIRED).isEmpty());
    }

    @Test
    public void cooldownAllowsFireExactlyAtTickPlusCooldown() {
        UnitSpec unit = new UnitSpec("U", 1, 100, 1L, 1);
        ScenarioSpec scenario = scenario(100L, 1, 7, Collections.singletonList(unit),
                Collections.singletonList(tower("T", 1L, 99L, 1, 3)));

        List<SimulationEvent> shots = events(run(scenario), SimulationEventType.TOWER_FIRED);

        assertEquals(Arrays.asList(0, 3, 6), ticks(shots));
    }

    @Test
    public void simultaneousEqualUnitsUseDeploymentIdNotInputOrder() {
        UnitSpec unit = new UnitSpec("U", 1, 10, 1L, 1);
        ScenarioSpec scenario = scenario(100L, 1, 1, Collections.singletonList(unit),
                Collections.singletonList(tower("T", 1L, 100L, 1, 10)));
        DeploymentPlan reversed = DeploymentPlan.of(
                new Deployment("B", "U", 0), new Deployment("A", "U", 0));

        SimulationEvent shot = events(run(scenario, reversed),
                SimulationEventType.TOWER_FIRED).get(0);

        assertEquals("A", shot.getDeploymentId());
    }

    @Test
    public void breachWinsOverShotAndTimeoutInSameTick() {
        UnitSpec unit = new UnitSpec("U", 1, 10, 5L, 1);
        ScenarioSpec scenario = scenario(5L, 1, 1, Collections.singletonList(unit),
                Collections.singletonList(tower("T", 5L, 0L, 99, 1)));

        SimulationResult result = run(scenario);

        assertEquals(SimulationStatus.VICTORY, result.getResult());
        assertEquals(0, result.getEndTick());
        assertTrue(events(result, SimulationEventType.TOWER_FIRED).isEmpty());
        assertEquals(Arrays.asList(
                SimulationEventType.UNIT_SPAWNED,
                SimulationEventType.UNIT_ENTERED_TOWER_RANGE,
                SimulationEventType.UNIT_BREACHED,
                SimulationEventType.BASE_DAMAGED,
                SimulationEventType.SIMULATION_FINISHED), eventTypes(result));
    }

    @Test
    public void towersFireByIdAndImmediatelyExcludeLethalTarget() {
        UnitSpec unit = new UnitSpec("U", 1, 50, 1L, 1);
        ScenarioSpec scenario = scenario(100L, 1, 1, Collections.singletonList(unit),
                Arrays.asList(
                        tower("B_TOWER", 1L, 100L, 50, 1),
                        tower("A_TOWER", 1L, 100L, 50, 1)));
        DeploymentPlan plan = DeploymentPlan.of(
                new Deployment("B_UNIT", "U", 0),
                new Deployment("A_UNIT", "U", 0));

        List<SimulationEvent> shots = events(run(scenario, plan),
                SimulationEventType.TOWER_FIRED);

        assertEquals(2, shots.size());
        assertEquals("A_TOWER", shots.get(0).getTowerId());
        assertEquals("A_UNIT", shots.get(0).getDeploymentId());
        assertEquals("B_TOWER", shots.get(1).getTowerId());
        assertEquals("B_UNIT", shots.get(1).getDeploymentId());
    }

    @Test
    public void simultaneousBreachesUseDeploymentIdAndStopAtVictory() {
        UnitSpec unit = new UnitSpec("U", 1, 10, 1L, 1);
        ScenarioSpec scenario = scenario(1L, 1, 2, Collections.singletonList(unit),
                Collections.<TowerSpec>emptyList());
        DeploymentPlan reversed = DeploymentPlan.of(
                new Deployment("B", "U", 0), new Deployment("A", "U", 0));

        SimulationResult result = run(scenario, reversed);
        List<SimulationEvent> breaches = events(result, SimulationEventType.UNIT_BREACHED);

        assertEquals(SimulationStatus.VICTORY, result.getResult());
        assertEquals(1, breaches.size());
        assertEquals("A", breaches.get(0).getDeploymentId());
        assertEquals("B", result.getEvents().get(1).getDeploymentId());
    }

    @Test
    public void defeatWinsOverTimeoutWhenLastUnitDiesOnFinalTick() {
        UnitSpec unit = new UnitSpec("U", 1, 1, 1L, 1);
        ScenarioSpec scenario = scenario(100L, 1, 1, Collections.singletonList(unit),
                Collections.singletonList(tower("T", 1L, 100L, 1, 1)));

        assertEquals(SimulationStatus.DEFEAT, run(scenario).getResult());
    }

    @Test
    public void snapshotsExposeOnlyIntegerProgressAndAdvanceOneTickAtATime() {
        UnitSpec unit = new UnitSpec("U", 1, 10, 7L, 1);
        ScenarioSpec scenario = scenario(100L, 1, 10, Collections.singletonList(unit),
                Collections.<TowerSpec>emptyList());
        CombatSimulation simulation = new CombatSimulation(scenario, defaultPlan());

        SimulationState first = simulation.advanceOneTick();
        SimulationState second = simulation.advanceOneTick();

        assertEquals(1, first.getCurrentTick());
        assertEquals(7L, first.getActiveUnits().get(0).getRouteProgressUnits());
        assertEquals(2, second.getCurrentTick());
        assertEquals(14L, second.getActiveUnits().get(0).getRouteProgressUnits());
        assertFalse(second.getStatus().isTerminal());
    }

    private ScenarioSpec singleUnitScenario(long speed, int health, int timeLimit,
                                            TowerSpec tower) {
        UnitSpec unit = new UnitSpec("U", 1, health, speed, 1);
        return scenario(100L, 1, timeLimit, Collections.singletonList(unit),
                Collections.singletonList(tower));
    }

    private ScenarioSpec scenario(long routeLength, int baseIntegrity, int timeLimit,
                                  List<UnitSpec> units, List<TowerSpec> towers) {
        return new ScenarioSpec("TEST", routeLength, baseIntegrity, 10, 3,
                60, 1, timeLimit, units, towers);
    }

    private TowerSpec tower(String id, long position, long range, int damage, int cooldown) {
        return new TowerSpec(id, position, range, damage, cooldown,
                TargetRule.FURTHEST_PROGRESS);
    }

    private SimulationResult run(ScenarioSpec scenario) {
        return run(scenario, defaultPlan());
    }

    private SimulationResult run(ScenarioSpec scenario, DeploymentPlan plan) {
        return new CombatSimulation(scenario, plan).runToCompletion();
    }

    private DeploymentPlan defaultPlan() {
        return DeploymentPlan.of(new Deployment("U1", "U", 0));
    }

    private List<SimulationEvent> events(SimulationResult result,
                                         SimulationEventType type) {
        List<SimulationEvent> matches = new ArrayList<>();
        for (SimulationEvent event : result.getEvents()) {
            if (event.getType() == type) matches.add(event);
        }
        return matches;
    }

    private List<Integer> ticks(List<SimulationEvent> events) {
        List<Integer> ticks = new ArrayList<>();
        for (SimulationEvent event : events) ticks.add(event.getTick());
        return ticks;
    }

    private List<SimulationEventType> eventTypes(SimulationResult result) {
        List<SimulationEventType> types = new ArrayList<>();
        for (SimulationEvent event : result.getEvents()) types.add(event.getType());
        return types;
    }
}
