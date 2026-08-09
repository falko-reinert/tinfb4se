package de.codecrunch.gameplay.simulation.plan;

import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Validates and normalizes deployment plans before any combat state is created. */
public final class PlanValidator {
    private static final Comparator<Deployment> CANONICAL_ORDER = new Comparator<Deployment>() {
        @Override
        public int compare(Deployment left, Deployment right) {
            int byTick = Integer.compare(left.getSpawnTick(), right.getSpawnTick());
            if (byTick != 0) return byTick;
            String leftId = left.getDeploymentId();
            String rightId = right.getDeploymentId();
            if (leftId == null) return rightId == null ? 0 : -1;
            if (rightId == null) return 1;
            return leftId.compareTo(rightId);
        }
    };

    public PlanValidationResult validateAndNormalize(ScenarioSpec scenario,
                                                     DeploymentPlan plan) {
        if (scenario == null) {
            throw new IllegalArgumentException("scenario is required");
        }

        List<PlanValidationError> errors = new ArrayList<>();
        List<Deployment> source = plan == null
                ? Collections.<Deployment>emptyList() : plan.getDeployments();
        if (source.isEmpty()) addError(errors, PlanValidationError.EMPTY_PLAN);
        if (source.size() > scenario.getMaxDeployments()) {
            addError(errors, PlanValidationError.TOO_MANY_DEPLOYMENTS);
        }

        int earliestSpawnTick = Integer.MAX_VALUE;
        long spentBudget = 0L;
        Set<String> ids = new HashSet<>();
        for (Deployment deployment : source) {
            if (deployment == null) {
                addError(errors, PlanValidationError.MISSING_DEPLOYMENT_ID);
                addError(errors, PlanValidationError.UNKNOWN_UNIT_TYPE);
                continue;
            }
            String deploymentId = deployment.getDeploymentId();
            if (deploymentId == null || deploymentId.trim().isEmpty()) {
                addError(errors, PlanValidationError.MISSING_DEPLOYMENT_ID);
            } else if (!ids.add(deploymentId)) {
                addError(errors, PlanValidationError.DUPLICATE_DEPLOYMENT_ID);
            }

            UnitSpec unitSpec = scenario.getUnitSpec(deployment.getUnitTypeId());
            if (unitSpec == null) {
                addError(errors, PlanValidationError.UNKNOWN_UNIT_TYPE);
            } else {
                spentBudget += unitSpec.getCost();
            }

            int spawnTick = deployment.getSpawnTick();
            if (spawnTick < 0) addError(errors, PlanValidationError.SPAWN_OUTSIDE_WINDOW);
            if (spawnTick % scenario.getSpawnGridTicks() != 0) {
                addError(errors, PlanValidationError.SPAWN_OFF_GRID);
            }
            earliestSpawnTick = Math.min(earliestSpawnTick, spawnTick);
        }
        if (spentBudget > scenario.getBudget()) {
            addError(errors, PlanValidationError.BUDGET_EXCEEDED);
        }

        int offset = earliestSpawnTick == Integer.MAX_VALUE || earliestSpawnTick < 0
                ? 0 : earliestSpawnTick;
        List<Deployment> normalized = new ArrayList<>();
        for (Deployment deployment : source) {
            if (deployment == null) continue;
            int normalizedTick = deployment.getSpawnTick() - offset;
            if (normalizedTick < 0 || normalizedTick > scenario.getMaxSpawnTick()) {
                addError(errors, PlanValidationError.SPAWN_OUTSIDE_WINDOW);
            }
            normalized.add(new Deployment(deployment.getDeploymentId(),
                    deployment.getUnitTypeId(), normalizedTick));
        }
        Collections.sort(normalized, CANONICAL_ORDER);
        return new PlanValidationResult(new DeploymentPlan(normalized), errors);
    }

    private void addError(List<PlanValidationError> errors, PlanValidationError error) {
        if (!errors.contains(error)) errors.add(error);
    }
}
