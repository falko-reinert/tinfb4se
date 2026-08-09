package de.codecrunch.gameplay.planning;

import de.codecrunch.gameplay.simulation.ReferenceScenario;
import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.plan.PlanValidationResult;
import de.codecrunch.gameplay.simulation.plan.PlanValidator;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * UI-facing planning state. Costs, limits and validity come from the immutable scenario
 * and the shared {@link PlanValidator}; this class contains no combat rules.
 */
public final class AttackPlanningSession {
    private final ScenarioSpec scenario;
    private final PlanValidator validator = new PlanValidator();
    private final Slot[] slots;
    private boolean submitted;
    private DeploymentPlan submittedPlan;

    public AttackPlanningSession(ScenarioSpec scenario) {
        if (scenario == null) throw new IllegalArgumentException("scenario is required");
        this.scenario = scenario;
        this.slots = new Slot[scenario.getMaxDeployments()];
        for (int index = 0; index < slots.length; index++) slots[index] = new Slot();
        applyReferenceDefaults();
    }

    public void cycleUnitType(int slotIndex) {
        if (submitted) return;
        Slot slot = slot(slotIndex);
        if (slot.unitTypeId == null) slot.unitTypeId = ReferenceScenario.BULWARK;
        else if (ReferenceScenario.BULWARK.equals(slot.unitTypeId)) {
            slot.unitTypeId = ReferenceScenario.RUNNER;
        } else {
            slot.unitTypeId = null;
        }
    }

    public void adjustSpawnTick(int slotIndex, int gridSteps) {
        if (submitted || gridSteps == 0) return;
        Slot slot = slot(slotIndex);
        long requested = (long) slot.spawnTick
                + (long) gridSteps * scenario.getSpawnGridTicks();
        slot.spawnTick = (int) Math.max(0L,
                Math.min((long) scenario.getMaxSpawnTick(), requested));
    }

    public boolean submit() {
        if (submitted) return true;
        PlanValidationResult validation = validate();
        if (!validation.isValid()) return false;
        submittedPlan = validation.getNormalizedPlan();
        submitted = true;
        return true;
    }

    public PlanningSnapshot snapshot() {
        PlanValidationResult validation = validate();
        List<PlanningSlotView> slotViews = new ArrayList<>();
        int spentBudget = 0;
        for (int index = 0; index < slots.length; index++) {
            Slot slot = slots[index];
            UnitSpec spec = slot.unitTypeId == null
                    ? null : scenario.getUnitSpec(slot.unitTypeId);
            int cost = spec == null ? 0 : spec.getCost();
            spentBudget += cost;
            slotViews.add(new PlanningSlotView(index, slot.unitTypeId,
                    slot.spawnTick, cost));
        }
        return new PlanningSnapshot(scenario, slotViews, spentBudget,
                validation.getErrors(), submitted, submittedPlan);
    }

    public DeploymentPlan getSubmittedPlan() {
        return submittedPlan;
    }

    private PlanValidationResult validate() {
        return validator.validateAndNormalize(scenario, currentPlan());
    }

    private DeploymentPlan currentPlan() {
        List<Deployment> deployments = new ArrayList<>();
        for (int index = 0; index < slots.length; index++) {
            Slot slot = slots[index];
            if (slot.unitTypeId == null) continue;
            deployments.add(new Deployment("SLOT_" + (index + 1),
                    slot.unitTypeId, slot.spawnTick));
        }
        return new DeploymentPlan(deployments);
    }

    private Slot slot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.length) {
            throw new IllegalArgumentException("Unknown slot: " + slotIndex);
        }
        return slots[slotIndex];
    }

    private void applyReferenceDefaults() {
        if (slots.length < 3
                || scenario.getUnitSpec(ReferenceScenario.BULWARK) == null
                || scenario.getUnitSpec(ReferenceScenario.RUNNER) == null) return;
        slots[0].unitTypeId = ReferenceScenario.BULWARK;
        slots[1].unitTypeId = ReferenceScenario.RUNNER;
        slots[1].spawnTick = 90;
        slots[2].unitTypeId = ReferenceScenario.RUNNER;
        slots[2].spawnTick = 180;
    }

    private static final class Slot {
        private String unitTypeId;
        private int spawnTick;
    }
}
