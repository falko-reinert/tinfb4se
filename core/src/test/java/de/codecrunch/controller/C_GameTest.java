package de.codecrunch.controller;

import org.junit.Test;

import de.codecrunch.gameplay.GameClock;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;

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
    public void validMapCreatesAndUpdatesTheSliceEnemy() {
        M_Map map = horizontalMap();
        C_Game game = new C_Game(map, new RecordingGameClock());

        game.update(0.5f);

        assertNotNull(game.getEnemy());
        assertEquals(2f, game.getEnemy().getX(), 0.001f);
        assertEquals(1f, game.getEnemy().getY(), 0.001f);
    }

    @Test
    public void incompleteMapDoesNotCreateAnEnemy() {
        C_Game game = new C_Game(new M_Map(), new RecordingGameClock());

        assertNull(game.getEnemy());
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
