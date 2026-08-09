package de.codecrunch.gameplay.planning;

import de.codecrunch.gameplay.simulation.ReferenceScenario;
import de.codecrunch.gameplay.simulation.plan.PlanValidationError;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AttackPlanningSessionTest {
    @Test
    public void referenceDefaultsExposeThreeValidSlotsAndCentralCosts() {
        AttackPlanningSession session = new AttackPlanningSession(ReferenceScenario.create());

        PlanningSnapshot snapshot = session.snapshot();

        assertEquals(3, snapshot.getSlots().size());
        assertEquals(ReferenceScenario.BULWARK,
                snapshot.getSlots().get(0).getUnitTypeId());
        assertEquals(ReferenceScenario.RUNNER,
                snapshot.getSlots().get(1).getUnitTypeId());
        assertEquals(90, snapshot.getSlots().get(1).getSpawnTick());
        assertEquals(180, snapshot.getSlots().get(2).getSpawnTick());
        assertEquals(5, snapshot.getSpentBudget());
        assertEquals(0, snapshot.getRemainingBudget());
        assertTrue(snapshot.isValid());
    }

    @Test
    public void unitSelectionMakesBudgetErrorsImmediatelyVisible() {
        AttackPlanningSession session = new AttackPlanningSession(ReferenceScenario.create());
        session.cycleUnitType(1); // RUNNER -> empty
        session.cycleUnitType(1); // empty -> BULWARK

        PlanningSnapshot snapshot = session.snapshot();

        assertEquals(7, snapshot.getSpentBudget());
        assertFalse(snapshot.isValid());
        assertTrue(snapshot.getValidationErrors().contains(
                PlanValidationError.BUDGET_EXCEEDED));
        assertFalse(session.submit());
    }

    @Test
    public void timeControlsUseGridAndClampToPlanningWindow() {
        AttackPlanningSession session = new AttackPlanningSession(ReferenceScenario.create());

        session.adjustSpawnTick(1, 1);
        assertEquals(105, session.snapshot().getSlots().get(1).getSpawnTick());
        session.adjustSpawnTick(1, 100);
        assertEquals(360, session.snapshot().getSlots().get(1).getSpawnTick());
        session.adjustSpawnTick(1, -100);
        assertEquals(0, session.snapshot().getSlots().get(1).getSpawnTick());
    }

    @Test
    public void emptyPlanIsRejectedBySharedPlanValidator() {
        AttackPlanningSession session = new AttackPlanningSession(ReferenceScenario.create());
        session.cycleUnitType(0); // BULWARK -> RUNNER
        session.cycleUnitType(0); // RUNNER -> empty
        session.cycleUnitType(1); // RUNNER -> empty
        session.cycleUnitType(2); // RUNNER -> empty

        assertTrue(session.snapshot().getValidationErrors().contains(
                PlanValidationError.EMPTY_PLAN));
        assertFalse(session.submit());
    }

    @Test
    public void submissionLocksAndExposesNormalizedPlanWithoutRunningCombat() {
        AttackPlanningSession session = new AttackPlanningSession(ReferenceScenario.create());

        assertTrue(session.submit());
        session.adjustSpawnTick(1, 1);
        session.cycleUnitType(0);

        PlanningSnapshot snapshot = session.snapshot();
        assertTrue(snapshot.isSubmitted());
        assertNotNull(snapshot.getSubmittedPlan());
        assertEquals(3, snapshot.getSubmittedPlan().getDeployments().size());
        assertEquals(90, snapshot.getSlots().get(1).getSpawnTick());
        assertEquals(ReferenceScenario.BULWARK,
                snapshot.getSlots().get(0).getUnitTypeId());
    }
}
