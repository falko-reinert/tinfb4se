package de.codecrunch.gameplay.simulation;

import de.codecrunch.gameplay.simulation.plan.Deployment;
import de.codecrunch.gameplay.simulation.plan.DeploymentPlan;
import de.codecrunch.gameplay.simulation.spec.ScenarioSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Runs the reference search space and evaluates every documented balancing gate. */
public final class ReferenceBatchAnalyzer {
    private static final String BRR = "BULWARK+RUNNER+RUNNER";
    private static final String RRR = "RUNNER+RUNNER+RUNNER";

    private final PlanEnumerator enumerator = new PlanEnumerator();
    private final PlanBatchEvaluator evaluator = new PlanBatchEvaluator();

    public ReferenceBatchReport analyze(int determinismRepetitions) {
        ScenarioSpec scenario = ReferenceScenario.create();
        List<DeploymentPlan> allPlans = enumerator.enumerateAllValidPlans(scenario);
        BatchEvaluation all = evaluator.evaluate(scenario, allPlans, determinismRepetitions);
        List<DeploymentPlan> anchored = enumerator.enumerateAnchoredComposition(scenario,
                Arrays.asList(ReferenceScenario.BULWARK,
                        ReferenceScenario.RUNNER, ReferenceScenario.RUNNER));

        int anchoredVictories = 0;
        int anchoredDefeats = 0;
        int anchoredTimeouts = 0;
        for (DeploymentPlan plan : anchored) {
            BatchPlanResult outcome = all.getPlan(enumerator.canonicalKey(plan));
            if (outcome == null) {
                throw new IllegalStateException("Anchored plan missing from full search space");
            }
            if (outcome.getResult() == SimulationStatus.VICTORY) anchoredVictories++;
            else if (outcome.getResult() == SimulationStatus.DEFEAT) anchoredDefeats++;
            else if (outcome.getResult() == SimulationStatus.TIMEOUT) anchoredTimeouts++;
        }

        String[] robustPair = findAdjacentWinningPair(scenario, anchored, all);
        CompositionSummary brr = all.getComposition(BRR);
        CompositionSummary rrr = all.getComposition(RRR);
        BalancingGateReport gates = new BalancingGateReport(
                all.getVictories() > 0 && all.getDefeats() + all.getTimeouts() > 0,
                brr != null && brr.getVictories() > 0 && brr.getNonVictories() > 0,
                rrr != null && rrr.getVictories() == 0,
                all.getVictories() > 1,
                robustPair[0] != null,
                determinismRepetitions >= 100 && all.hasDeterministicEventLogs(),
                all.hasIncrementalExecutionParity()
                        && all.isInputCollectionOrderIndependent());
        return new ReferenceBatchReport(all, anchored.size(), anchoredVictories,
                anchoredDefeats, anchoredTimeouts, robustPair[0], robustPair[1], gates);
    }

    private String[] findAdjacentWinningPair(ScenarioSpec scenario,
                                             List<DeploymentPlan> anchored,
                                             BatchEvaluation evaluation) {
        for (DeploymentPlan plan : anchored) {
            String originalKey = enumerator.canonicalKey(plan);
            BatchPlanResult original = evaluation.getPlan(originalKey);
            if (original.getResult() != SimulationStatus.VICTORY) continue;

            for (int index = 0; index < plan.getDeployments().size(); index++) {
                Deployment deployment = plan.getDeployments().get(index);
                if (!ReferenceScenario.RUNNER.equals(deployment.getUnitTypeId())) continue;
                for (int direction : new int[]{-1, 1}) {
                    int shiftedTick = deployment.getSpawnTick()
                            + direction * scenario.getSpawnGridTicks();
                    if (shiftedTick < 0 || shiftedTick > scenario.getMaxSpawnTick()) continue;
                    DeploymentPlan shifted = shift(plan, index, shiftedTick);
                    String shiftedKey = enumerator.canonicalKey(shifted);
                    BatchPlanResult shiftedResult = evaluation.getPlan(shiftedKey);
                    if (shiftedResult != null
                            && shiftedResult.getResult() == SimulationStatus.VICTORY) {
                        return new String[]{originalKey, shiftedKey};
                    }
                }
            }
        }
        return new String[]{null, null};
    }

    private DeploymentPlan shift(DeploymentPlan plan, int shiftedIndex, int shiftedTick) {
        List<Deployment> shifted = new ArrayList<>();
        for (int index = 0; index < plan.getDeployments().size(); index++) {
            Deployment source = plan.getDeployments().get(index);
            shifted.add(new Deployment(source.getDeploymentId(), source.getUnitTypeId(),
                    index == shiftedIndex ? shiftedTick : source.getSpawnTick()));
        }
        return new DeploymentPlan(shifted);
    }
}
