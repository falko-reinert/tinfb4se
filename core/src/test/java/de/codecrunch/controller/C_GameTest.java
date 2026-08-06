package de.codecrunch.controller;

import org.junit.Test;

import de.codecrunch.gameplay.GameClock;

import static org.junit.Assert.assertTrue;

public class C_GameTest {
    @Test
    public void disposeStopsTheOwnedGameClock() {
        RecordingGameClock clock = new RecordingGameClock();
        C_Game game = new C_Game(null, clock);

        game.dispose();

        assertTrue(clock.stopped);
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
