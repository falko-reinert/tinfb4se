package de.codecrunch.gameplay.simulation;

/** Domain events emitted in the exact order in which rules are applied. */
public enum SimulationEventType {
    UNIT_SPAWNED,
    UNIT_ENTERED_TOWER_RANGE,
    TOWER_FIRED,
    UNIT_DAMAGED,
    UNIT_DEFEATED,
    UNIT_BREACHED,
    BASE_DAMAGED,
    SIMULATION_FINISHED
}
