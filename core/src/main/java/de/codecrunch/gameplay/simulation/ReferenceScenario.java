package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import de.codecrunch.gameplay.simulation.spec.TargetRule;
import de.codecrunch.gameplay.simulation.spec.TowerSpec;
import de.codecrunch.gameplay.simulation.spec.UnitSpec;

import java.util.Arrays;
import java.util.Collections;

/** Data object for the specification's first scenario, "Das Zeitfenster". */
public final class ReferenceScenario {
    public static final String BULWARK = "BULWARK";
    public static final String RUNNER = "RUNNER";

    private ReferenceScenario() {
    }

    public static ScenarioSpec create() {
        UnitSpec bulwark = new UnitSpec(BULWARK, 3, 200, 150L, 1);
        UnitSpec runner = new UnitSpec(RUNNER, 1, 50, 350L, 1);
        TowerSpec guardian = new TowerSpec("GUARDIAN_1", 30000L, 12000L,
                50, 96, TargetRule.FURTHEST_PROGRESS);
        return new ScenarioSpec("THE_TIMING_WINDOW", 60000L, 3, 5, 3,
                360, 15, 900, Arrays.asList(bulwark, runner),
                Collections.singletonList(guardian));
    }

    public static Deployment deployment(String deploymentId, String unitTypeId,
                                        int spawnTick) {
        return new Deployment(deploymentId, unitTypeId, spawnTick);
    }

    public static DeploymentPlan plan(Deployment... deployments) {
        return DeploymentPlan.of(deployments);
    }
}
