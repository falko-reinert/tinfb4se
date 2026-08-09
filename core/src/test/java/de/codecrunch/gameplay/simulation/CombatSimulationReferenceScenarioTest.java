package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static de.codecrunch.gameplay.simulation.ReferenceScenario.BULWARK;
import static de.codecrunch.gameplay.simulation.ReferenceScenario.RUNNER;
import static de.codecrunch.gameplay.simulation.ReferenceScenario.deployment;
import static de.codecrunch.gameplay.simulation.ReferenceScenario.plan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CombatSimulationReferenceScenarioTest {
    @Test
    public void referenceTimingPlanWinsWithExpectedKeyEventTicks() {
        SimulationResult result = run(plan(
                deployment("B", BULWARK, 0),
                deployment("R1", RUNNER, 90),
                deployment("R2", RUNNER, 180)));

        assertEquals(SimulationStatus.VICTORY, result.getResult());
        assertEquals(0, result.getRemainingBaseIntegrity());
        assertEquals(399, result.getEndTick());
        assertEquals(java.util.Arrays.asList(119, 215),
                ticksOf(result, SimulationEventType.TOWER_FIRED));
        assertEquals(java.util.Arrays.asList(261, 351, 399),
                ticksOf(result, SimulationEventType.UNIT_BREACHED));
    }

    @Test
    public void earlyRunnersOvertakeAndLose() {
        SimulationResult result = run(plan(
                deployment("B", BULWARK, 0),
                deployment("R1", RUNNER, 60),
                deployment("R2", RUNNER, 90)));

        assertEquals(SimulationStatus.DEFEAT, result.getResult());
        assertEquals(2, count(result, SimulationEventType.UNIT_DEFEATED));
        assertEquals(1, count(result, SimulationEventType.UNIT_BREACHED));
    }

    @Test
    public void threeRunnersLoseWithoutDamageSink() {
        SimulationResult result = run(plan(
                deployment("R1", RUNNER, 0),
                deployment("R2", RUNNER, 90),
                deployment("R3", RUNNER, 180)));

        assertEquals(SimulationStatus.DEFEAT, result.getResult());
        assertEquals(3, count(result, SimulationEventType.UNIT_DEFEATED));
        assertEquals(0, count(result, SimulationEventType.UNIT_BREACHED));
    }

    @Test
    public void twoBreachesCannotDefeatBaseIntegrityThree() {
        SimulationResult result = run(plan(
                deployment("B", BULWARK, 0),
                deployment("R", RUNNER, 90)));

        assertEquals(SimulationStatus.DEFEAT, result.getResult());
        assertEquals(1, result.getRemainingBaseIntegrity());
        assertEquals(2, count(result, SimulationEventType.UNIT_BREACHED));
    }

    @Test
    public void twoBulwarksAreInvalidBecauseTheyExceedBudget() {
        SimulationResult result = run(plan(
                deployment("B1", BULWARK, 0),
                deployment("B2", BULWARK, 90)));

        assertEquals(SimulationStatus.INVALID_PLAN, result.getResult());
        assertTrue(result.getValidationErrors().contains(
                de.codecrunch.gameplay.simulation.plan.PlanValidationError.BUDGET_EXCEEDED));
        assertFalse(hasEvent(result, SimulationEventType.UNIT_SPAWNED));
    }

    @Test
    public void oneHundredRunsProduceIdenticalCanonicalEventLogHash() {
        DeploymentPlan plan = plan(
                deployment("B", BULWARK, 0),
                deployment("R1", RUNNER, 90),
                deployment("R2", RUNNER, 180));
        String expectedHash = run(plan).eventLogHash();

        for (int repetition = 0; repetition < 100; repetition++) {
            assertEquals(expectedHash, run(plan).eventLogHash());
        }
    }

    @Test
    public void incrementalAndHeadlessExecutionHaveByteIdenticalLogs() {
        DeploymentPlan plan = plan(
                deployment("B", BULWARK, 0),
                deployment("R1", RUNNER, 90),
                deployment("R2", RUNNER, 180));
        SimulationResult headless = run(plan);
        CombatSimulation incremental = new CombatSimulation(ReferenceScenario.create(), plan);

        while (!incremental.getState().getStatus().isTerminal()) {
            incremental.advanceOneTick();
        }

        assertEquals(headless.canonicalEventLog(),
                incremental.getResult().canonicalEventLog());
    }

    private SimulationResult run(DeploymentPlan plan) {
        return new CombatSimulation(ReferenceScenario.create(), plan).runToCompletion();
    }

    private List<Integer> ticksOf(SimulationResult result, SimulationEventType type) {
        List<Integer> ticks = new ArrayList<>();
        for (SimulationEvent event : result.getEvents()) {
            if (event.getType() == type) ticks.add(event.getTick());
        }
        return ticks;
    }

    private int count(SimulationResult result, SimulationEventType type) {
        return ticksOf(result, type).size();
    }

    private boolean hasEvent(SimulationResult result, SimulationEventType type) {
        return count(result, type) > 0;
    }
}
