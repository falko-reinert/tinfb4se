package de.codecrunch.gameplay.simulation;

/** Lifecycle state and terminal result of a combat simulation. */
public enum SimulationStatus {
    RUNNING,
    VICTORY,
    DEFEAT,
    TIMEOUT,
    INVALID_PLAN;

    public boolean isTerminal() {
        return this != RUNNING;
    }
}
