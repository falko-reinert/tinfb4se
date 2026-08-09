package de.codecrunch.gameplay.simulation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReferenceBatchAnalyzerTest {
    @Test
    public void referenceSearchConfirmsDocumentedTimingCountsAndAllGates() {
        ReferenceBatchReport report = new ReferenceBatchAnalyzer().analyze(100);

        assertEquals(1326, report.getAllPlans().getTotalPlans());
        assertEquals(325, report.getAnchoredTimingPlans());
        assertEquals(21, report.getAnchoredTimingVictories());
        assertEquals(304, report.getAnchoredTimingDefeats());
        assertEquals(0, report.getAnchoredTimingTimeouts());
        assertEquals(21, report.getAllPlans().getVictories());
        assertEquals(1305, report.getAllPlans().getDefeats());
        assertEquals(0, report.getAllPlans().getTimeouts());
        assertEquals(0, report.getAllPlans().getComposition(
                "RUNNER+RUNNER+RUNNER").getVictories());
        assertEquals(925, report.getAllPlans().getComposition(
                "BULWARK+RUNNER+RUNNER").getTotalPlans());
        assertEquals(21, report.getAllPlans().getComposition(
                "BULWARK+RUNNER+RUNNER").getVictories());
        assertEquals(SimulationStatus.VICTORY, report.getAllPlans().getPlan(
                "BULWARK@0;RUNNER@75;RUNNER@180").getResult());
        assertEquals(SimulationStatus.VICTORY, report.getAllPlans().getPlan(
                "BULWARK@0;RUNNER@90;RUNNER@165").getResult());
        assertNotNull(report.getRobustWinningPlan());
        assertNotNull(report.getAdjacentWinningPlan());
        assertTrue(report.getGates().hasWinningAndLosingPlan());
        assertTrue(report.getGates().bulwarkRunnerRunnerDependsOnTiming());
        assertTrue(report.getGates().threeRunnersNeverWin());
        assertTrue(report.getGates().hasMultipleWinningPlans());
        assertTrue(report.getGates().hasAdjacentRobustWinningPlan());
        assertTrue(report.getGates().eventLogsRepeatOneHundredTimes());
        assertTrue(report.getGates().independentOfExecutionAndCollectionOrder());
        assertTrue(report.getGates().allPassed());
    }
}
