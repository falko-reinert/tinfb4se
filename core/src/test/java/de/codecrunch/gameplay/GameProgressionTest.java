package de.codecrunch.gameplay;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameProgressionTest {
    private GameEconomy economy;
    private List<Integer> ticks;
    private GameProgression progression;

    @Before
    public void setUp() {
        economy = new GameEconomy();
        ticks = new ArrayList<>();
        progression = new GameProgression(economy, ticks::add);
    }

    @Test
    public void eachAdvanceCreditsIncomeAndUsesSequentialTickNumbers() {
        progression.advance();
        progression.advance();

        assertEquals(100, economy.getBalance());
        assertEquals(Arrays.asList(0, 1), ticks);
        assertEquals(2, progression.getTickCount());
    }

    @Test
    public void incomeIsAvailableToTheTickHandler() {
        List<Integer> observedBalances = new ArrayList<>();
        GameProgression observedProgression = new GameProgression(economy,
                tick -> observedBalances.add(economy.getBalance()));

        observedProgression.advance();

        assertEquals(Arrays.asList(50), observedBalances);
    }

    @Test
    public void economyPreservesAffordabilityAndSpendingBehavior() {
        economy.credit(100);

        assertTrue(economy.canAfford(100));
        economy.spend(120);

        assertEquals(-20, economy.getBalance());
        assertFalse(economy.canAfford(1));
    }
}
