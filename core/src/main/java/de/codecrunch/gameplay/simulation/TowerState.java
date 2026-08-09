package de.codecrunch.gameplay.simulation;

/** Immutable public snapshot of one tower cooldown. */
public final class TowerState {
    private final String towerId;
    private final int nextReadyTick;

    TowerState(String towerId, int nextReadyTick) {
        this.towerId = towerId;
        this.nextReadyTick = nextReadyTick;
    }

    public String getTowerId() {
        return towerId;
    }

    public int getNextReadyTick() {
        return nextReadyTick;
    }
}
