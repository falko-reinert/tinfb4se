package de.codecrunch.gameplay;

import java.util.function.IntConsumer;

public class GameProgression {
    private static final int INCOME_PER_TICK = 50;

    private final GameEconomy economy;
    private final IntConsumer tickHandler;
    private int tickCount;

    public GameProgression(GameEconomy economy, IntConsumer tickHandler) {
        this.economy = economy;
        this.tickHandler = tickHandler;
    }

    public void advance() {
        economy.credit(INCOME_PER_TICK);
        tickHandler.accept(tickCount);
        tickCount++;
    }

    public int getTickCount() {
        return tickCount;
    }
}
