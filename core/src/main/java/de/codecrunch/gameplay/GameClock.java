package de.codecrunch.gameplay;

public interface GameClock {
    void start(Runnable tick);

    void stop();
}
