package de.codecrunch.gameplay.simulation.plan;

/** One immutable entry in a deployment plan. */
public final class Deployment {
    private final String deploymentId;
    private final String unitTypeId;
    private final int spawnTick;

    public Deployment(String deploymentId, String unitTypeId, int spawnTick) {
        this.deploymentId = deploymentId;
        this.unitTypeId = unitTypeId;
        this.spawnTick = spawnTick;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public int getSpawnTick() {
        return spawnTick;
    }
}
