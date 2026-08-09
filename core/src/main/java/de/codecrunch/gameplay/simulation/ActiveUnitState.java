package de.codecrunch.gameplay.simulation;

/** Immutable public snapshot of one active unit. */
public final class ActiveUnitState {
    private final String deploymentId;
    private final String unitTypeId;
    private final int health;
    private final long routeProgressUnits;

    ActiveUnitState(String deploymentId, String unitTypeId, int health,
                    long routeProgressUnits) {
        this.deploymentId = deploymentId;
        this.unitTypeId = unitTypeId;
        this.health = health;
        this.routeProgressUnits = routeProgressUnits;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public int getHealth() {
        return health;
    }

    public long getRouteProgressUnits() {
        return routeProgressUnits;
    }
}
