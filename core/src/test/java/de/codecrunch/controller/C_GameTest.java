package de.codecrunch.controller;

import org.junit.Test;

import de.codecrunch.gameplay.GameClock;
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
    public void validMapCreatesAndUpdatesTheSliceUnit() {
        M_Map map = horizontalMap();
        C_Game game = new C_Game(map, new RecordingGameClock());

        game.update(0.5f);

        assertNotNull(game.getUnit());
        assertEquals(2f, game.getUnit().getX(), 0.001f);
        assertEquals(1f, game.getUnit().getY(), 0.001f);
    }

    @Test
    public void incompleteMapDoesNotCreateAUnit() {
        C_Game game = new C_Game(new M_Map(), new RecordingGameClock());

        assertNull(game.getUnit());
    }

    @Test
    public void progressionTickLetsDefendingTowerAttackTheRunningUnit() {
        C_Game game = new C_Game(horizontalMap(), new RecordingGameClock());
        M_SmallTower tower = new M_SmallTower();
        tower.setPos(2, 2);
        game.addDefendingTower(tower);

        game.tick(0);

        assertEquals(75, game.getUnit().getUnit().getCurrentLife());
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
