package de.codecrunch.gameplay.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable state snapshot returned after incremental simulation steps. */
public final class SimulationState {
    private final int currentTick;
    private final int baseIntegrity;
    private final SimulationStatus status;
    private final List<ActiveUnitState> activeUnits;
    private final List<TowerState> towerStates;
    private final List<SimulationEvent> eventLog;

    SimulationState(int currentTick, int baseIntegrity, SimulationStatus status,
                    List<ActiveUnitState> activeUnits, List<TowerState> towerStates,
                    List<SimulationEvent> eventLog) {
        this.currentTick = currentTick;
        this.baseIntegrity = baseIntegrity;
        this.status = status;
        this.activeUnits = immutableCopy(activeUnits);
        this.towerStates = immutableCopy(towerStates);
        this.eventLog = immutableCopy(eventLog);
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public int getBaseIntegrity() {
        return baseIntegrity;
    }

    public SimulationStatus getStatus() {
        return status;
    }

    public List<ActiveUnitState> getActiveUnits() {
        return activeUnits;
    }

    public List<TowerState> getTowerStates() {
        return towerStates;
    }

    public List<SimulationEvent> getEventLog() {
        return eventLog;
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return Collections.unmodifiableList(new ArrayList<>(source));
    }
}
