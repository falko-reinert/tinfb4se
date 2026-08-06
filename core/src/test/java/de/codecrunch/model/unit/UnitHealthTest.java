package de.codecrunch.model.unit;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitHealthTest {
    private UnitHealth health;

    @Before
    public void setUp() {
        health = new UnitHealth(100);
    }

    @Test
    public void startsAtMaximumLifeAndCapsAssignedLife() {
        assertEquals(100, health.getMaxLife());
        assertEquals(100, health.getCurrentLife());

        health.setCurrentLife(120);

        assertEquals(100, health.getCurrentLife());
    }

    @Test
    public void damageReducesLifeAndStopsAtZero() {
        health.takeDamage(40);
        assertEquals(60, health.getCurrentLife());

        health.takeDamage(80);
        assertEquals(0, health.getCurrentLife());
    }

    @Test
    public void healingCapsAtMaximumAndDoesNotRevive() {
        health.takeDamage(40);
        health.heal(80);
        assertEquals(100, health.getCurrentLife());

        health.takeDamage(100);
        health.heal(20);
        assertEquals(0, health.getCurrentLife());
    }

    @Test
    public void maximumLifeRejectsNonPositiveValues() {
        health.setMaxLife(0);
        assertEquals(100, health.getMaxLife());

        health.setMaxLife(-1);
        assertEquals(100, health.getMaxLife());

        health.setMaxLife(150);
        assertEquals(150, health.getMaxLife());
    }
}
