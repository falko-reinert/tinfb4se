package de.codecrunch.controller;

import org.junit.Test;

import de.codecrunch.gameplay.GameClock;
import de.codecrunch.gameplay.UnitOutcome;
import de.codecrunch.gameplay.WaveResult;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.M_SmallTower;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class C_GameTest {
    @Test
    public void disposeStopsTheOwnedGameClock() {
        RecordingGameClock clock = new RecordingGameClock();
        C_Game game = new C_Game(null, clock);

        game.dispose();

        assertTrue(clock.stopped);
    }

    @Test
    public void playerDeploymentCreatesAndUpdatesTheSliceUnit() {
        M_Map map = horizontalMap();
        C_Game game = new C_Game(map, new RecordingGameClock());
		game.deploySmallUnit();

        game.update(0.5f);

        assertNotNull(game.getUnit());
        assertEquals(2f, game.getUnit().getX(), 0.001f);
        assertEquals(1f, game.getUnit().getY(), 0.001f);
    }

    @Test
    public void incompleteMapDoesNotDeployAUnit() {
        C_Game game = new C_Game(new M_Map(), new RecordingGameClock());

		assertTrue(!game.deploySmallUnit());
        assertNull(game.getUnit());
    }

    @Test
    public void progressionTickFiresAfterTheShortAimingPhase() {
        C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());
		game.deploySmallUnit();
        M_SmallTower tower = new M_SmallTower();
        tower.setPos(2, 2);
        game.addDefendingTower(tower);

        game.tick(0);

        assertEquals(100, game.getUnit().getUnit().getCurrentLife());
        game.update(0.1f);
        assertEquals(100, game.getUnit().getUnit().getCurrentLife());
        game.update(0.2f);

        assertEquals(75, game.getUnit().getUnit().getCurrentLife());
    }

    @Test
    public void playerCanDeployAtMostThreeSmallUnits() {
        C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());

        assertEquals(3, game.getDeploymentsRemaining());
		assertTrue(game.deploySmallUnit());
		assertTrue(game.deploySmallUnit());
		assertTrue(game.deploySmallUnit());
		assertTrue(!game.deploySmallUnit());

        assertEquals(3, game.getUnits().size());
		assertEquals(0, game.getDeploymentsRemaining());
    }

	@Test
	public void playerCanDeployFastUnitWithItsOwnStats() {
		C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());

		assertTrue(game.deployFastUnit());
		assertEquals(200, game.getUnit().getUnit().getMaxLife());
		assertEquals(4, game.getUnit().getUnit().getSpeed());
		assertEquals(2, game.getDeploymentsRemaining());
	}

    @Test
    public void activeSpawnedUnitsFollowTheRouteWithWaveSpacing() {
        C_Game game = new C_Game(longHorizontalMap(), new RecordingGameClock());
		game.deploySmallUnit();

        game.update(0.5f);
		game.deploySmallUnit();

        game.update(0.25f);

        assertEquals(2, game.getUnits().size());
		assertEquals(2.5f, game.getUnits().get(0).getX(), 0.001f);
		assertEquals(1.5f, game.getUnits().get(1).getX(), 0.001f);
		assertEquals(3, game.getBaseIntegrity());
    }

    @Test
    public void unitThatReachesTheEndReducesBaseIntegrityOnceAndIsRemoved() {
        C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());
		game.deploySmallUnit();

        game.update(1f);
        game.update(1f);

        assertEquals(2, game.getBaseIntegrity());
        assertEquals(UnitOutcome.ESCAPED, game.getLastUnitOutcome());
        assertTrue(game.getUnits().isEmpty());
    }

    @Test
    public void defeatedUnitIsRemovedWithoutDamagingTheBase() {
        C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());
		game.deploySmallUnit();
        game.getUnit().getUnit().takeDamage(100);

        game.update(0.1f);

        assertEquals(3, game.getBaseIntegrity());
        assertEquals(UnitOutcome.DEFEATED, game.getLastUnitOutcome());
        assertTrue(game.getUnits().isEmpty());
    }

    @Test
    public void waveCompletesAfterEverySpawnedUnitIsDefeated() {
        C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());
		game.deploySmallUnit();
		game.deploySmallUnit();
		game.deploySmallUnit();
        for (int i = 0; i < 3; i++) {
            game.getUnits().get(0).getUnit().takeDamage(100);
            game.update(0f);
        }

        assertEquals(WaveResult.COMPLETE, game.getWaveResult());
        assertEquals(3, game.getBaseIntegrity());
    }

    @Test
    public void waveFailsWhenAllThreeUnitsReachTheBase() {
        C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());
		game.deploySmallUnit();
		game.deploySmallUnit();
		game.deploySmallUnit();

        game.update(2f);

        assertEquals(WaveResult.BASE_DESTROYED, game.getWaveResult());
        assertEquals(0, game.getBaseIntegrity());
    }

    private M_Map horizontalMap() {
        M_Map map = new M_Map();
        map.getTile(1, 1).setTileState(ME_TileState.START);
        map.getTile(2, 1).setTileState(ME_TileState.PATH_STRAIGHT);
        map.getTile(3, 1).setTileState(ME_TileState.END);
        map.setPath(Arrays.asList(
                map.getTile(1, 1), map.getTile(2, 1), map.getTile(3, 1)));
        return map;
    }

	private M_Map longHorizontalMap() {
		M_Map map = new M_Map();
		map.getTile(1, 1).setTileState(ME_TileState.START);
		map.getTile(2, 1).setTileState(ME_TileState.PATH_STRAIGHT);
		map.getTile(3, 1).setTileState(ME_TileState.PATH_STRAIGHT);
		map.getTile(4, 1).setTileState(ME_TileState.END);
		map.setPath(Arrays.asList(map.getTile(1, 1), map.getTile(2, 1),
				map.getTile(3, 1), map.getTile(4, 1)));
		return map;
	}

    private static class RecordingGameClock implements GameClock {
        private boolean stopped;

        @Override
        public void start(Runnable tick) {
        }

        @Override
        public void stop() {
            stopped = true;
        }
    }
}
