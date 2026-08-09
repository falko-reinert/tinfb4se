package de.codecrunch.gameplay.simulation;

/**
 * Immutable event record. Unused numeric fields contain {@code -1}; unused IDs and the
 * terminal result are {@code null}. The fixed field layout also defines canonical log
 * serialization.
 */
public final class SimulationEvent {
    private final int tick;
    private final SimulationEventType type;
    private final String deploymentId;
    private final String towerId;
    private final int amount;
    private final int remainingValue;
    private final long routeProgressUnits;
    private final SimulationStatus result;

    private SimulationEvent(int tick, SimulationEventType type, String deploymentId,
                            String towerId, int amount, int remainingValue,
                            long routeProgressUnits, SimulationStatus result) {
        this.tick = tick;
        this.type = type;
        this.deploymentId = deploymentId;
        this.towerId = towerId;
        this.amount = amount;
        this.remainingValue = remainingValue;
        this.routeProgressUnits = routeProgressUnits;
        this.result = result;
    }

    static SimulationEvent unit(int tick, SimulationEventType type, String deploymentId,
                                long routeProgressUnits) {
        return new SimulationEvent(tick, type, deploymentId, null,
                -1, -1, routeProgressUnits, null);
    }

    static SimulationEvent rangeEntered(int tick, String deploymentId, String towerId,
                                        long routeProgressUnits) {
        return new SimulationEvent(tick, SimulationEventType.UNIT_ENTERED_TOWER_RANGE,
                deploymentId, towerId, -1, -1, routeProgressUnits, null);
    }

    static SimulationEvent shot(int tick, SimulationEventType type, String deploymentId,
                                String towerId, int amount, int remainingValue,
                                long routeProgressUnits) {
        return new SimulationEvent(tick, type, deploymentId, towerId,
                amount, remainingValue, routeProgressUnits, null);
    }

    static SimulationEvent baseDamaged(int tick, String deploymentId, int amount,
                                       int remainingIntegrity, long routeProgressUnits) {
        return new SimulationEvent(tick, SimulationEventType.BASE_DAMAGED, deploymentId,
                null, amount, remainingIntegrity, routeProgressUnits, null);
    }

    static SimulationEvent finished(int tick, SimulationStatus result) {
        return new SimulationEvent(tick, SimulationEventType.SIMULATION_FINISHED,
                null, null, -1, -1, -1L, result);
    }

    public int getTick() {
        return tick;
    }

    public SimulationEventType getType() {
        return type;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getTowerId() {
        return towerId;
    }

    public int getAmount() {
        return amount;
    }

    public int getRemainingValue() {
        return remainingValue;
    }

    public long getRouteProgressUnits() {
        return routeProgressUnits;
    }

    public SimulationStatus getResult() {
        return result;
    }

    public String toCanonicalString() {
        return tick + "|" + type
                + "|deployment=" + value(deploymentId)
                + "|tower=" + value(towerId)
                + "|amount=" + numericValue(amount)
                + "|remaining=" + numericValue(remainingValue)
                + "|route=" + longValue(routeProgressUnits)
                + "|result=" + value(result == null ? null : result.name());
    }

    private String value(String value) {
        return value == null ? "-" : value;
    }

    private String numericValue(int value) {
        return value < 0 ? "-" : Integer.toString(value);
    }

    private String longValue(long value) {
        return value < 0 ? "-" : Long.toString(value);
    }
}
