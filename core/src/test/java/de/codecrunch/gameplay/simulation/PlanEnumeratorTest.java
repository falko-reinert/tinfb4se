package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.plan.PlanValidator;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlanEnumeratorTest {
    private final ScenarioSpec scenario = ReferenceScenario.create();
    private final PlanEnumerator enumerator = new PlanEnumerator();

    @Test
    public void anchoredBulwarkAndTwoRunnerTimingsHaveExactly325CanonicalPlans() {
        List<DeploymentPlan> plans = enumerator.enumerateAnchoredComposition(scenario,
                Arrays.asList(ReferenceScenario.BULWARK,
                        ReferenceScenario.RUNNER, ReferenceScenario.RUNNER));
        Set<String> keys = new HashSet<>();

        for (DeploymentPlan plan : plans) {
            assertTrue(new PlanValidator().validateAndNormalize(scenario, plan).isValid());
            keys.add(enumerator.canonicalKey(plan));
        }

        assertEquals(325, plans.size());
        assertEquals(plans.size(), keys.size());
    }

    @Test
    public void completeReferenceSearchContainsEveryCanonicalBudgetValidPlanOnce() {
        List<DeploymentPlan> plans = enumerator.enumerateAllValidPlans(scenario);
        Set<String> keys = new HashSet<>();
        for (DeploymentPlan plan : plans) keys.add(enumerator.canonicalKey(plan));

        assertEquals(1326, plans.size());
        assertEquals(plans.size(), keys.size());
    }
}
