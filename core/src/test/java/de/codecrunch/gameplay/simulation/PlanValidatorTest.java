package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.plan.PlanValidationError;
import de.codecrunch.gameplay.simulation.plan.PlanValidationResult;
import de.codecrunch.gameplay.simulation.plan.PlanValidator;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlanValidatorTest {
    private final ScenarioSpec scenario = ReferenceScenario.create();
    private final PlanValidator validator = new PlanValidator();

    @Test
    public void normalizesEarliestSpawnAndSortsByTickThenStableId() {
        DeploymentPlan source = DeploymentPlan.of(
                new Deployment("runner", ReferenceScenario.RUNNER, 90),
                new Deployment("bulwark", ReferenceScenario.BULWARK, 30));

        PlanValidationResult result = validator.validateAndNormalize(scenario, source);

        assertTrue(result.isValid());
        assertEquals("bulwark", result.getNormalizedPlan().getDeployments().get(0)
                .getDeploymentId());
        assertEquals(0, result.getNormalizedPlan().getDeployments().get(0).getSpawnTick());
        assertEquals(60, result.getNormalizedPlan().getDeployments().get(1).getSpawnTick());
    }

    @Test
    public void reportsConcreteErrorsWithoutPartiallyExecutingPlan() {
        DeploymentPlan invalid = DeploymentPlan.of(
                new Deployment("same", ReferenceScenario.BULWARK, 0),
                new Deployment("same", ReferenceScenario.BULWARK, 16),
                new Deployment("unknown", "MISSING", 390));

        PlanValidationResult validation = validator.validateAndNormalize(scenario, invalid);
        SimulationResult simulation = new CombatSimulation(scenario, invalid).runToCompletion();

        assertFalse(validation.isValid());
        assertTrue(validation.getErrors().contains(PlanValidationError.DUPLICATE_DEPLOYMENT_ID));
        assertTrue(validation.getErrors().contains(PlanValidationError.UNKNOWN_UNIT_TYPE));
        assertTrue(validation.getErrors().contains(PlanValidationError.SPAWN_OFF_GRID));
        assertTrue(validation.getErrors().contains(PlanValidationError.SPAWN_OUTSIDE_WINDOW));
        assertTrue(validation.getErrors().contains(PlanValidationError.BUDGET_EXCEEDED));
        assertEquals(SimulationStatus.INVALID_PLAN, simulation.getResult());
        assertEquals(1, simulation.getEvents().size());
        assertEquals(SimulationEventType.SIMULATION_FINISHED,
                simulation.getEvents().get(0).getType());
    }

    @Test
    public void rejectsEmptyAndOversizedPlans() {
        PlanValidationResult empty = validator.validateAndNormalize(scenario,
                new DeploymentPlan(java.util.Collections.<Deployment>emptyList()));
        PlanValidationResult oversized = validator.validateAndNormalize(scenario,
                DeploymentPlan.of(
                        new Deployment("a", ReferenceScenario.RUNNER, 0),
                        new Deployment("b", ReferenceScenario.RUNNER, 15),
                        new Deployment("c", ReferenceScenario.RUNNER, 30),
                        new Deployment("d", ReferenceScenario.RUNNER, 45)));

        assertTrue(empty.getErrors().contains(PlanValidationError.EMPTY_PLAN));
        assertTrue(oversized.getErrors().contains(PlanValidationError.TOO_MANY_DEPLOYMENTS));
    }
}
