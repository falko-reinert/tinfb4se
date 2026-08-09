package de.codecrunch.gameplay.simulation.spec;

/** Immutable combat values for one unit role. */
public final class UnitSpec {
    private final String unitTypeId;
    private final int cost;
    private final int maxHealth;
    private final long speedUnitsPerTick;
    private final int baseDamage;

    public UnitSpec(String unitTypeId, int cost, int maxHealth,
                    long speedUnitsPerTick, int baseDamage) {
        if (unitTypeId == null || unitTypeId.trim().isEmpty()) {
            throw new IllegalArgumentException("unitTypeId is required");
        }
        if (cost < 0 || maxHealth <= 0 || speedUnitsPerTick <= 0 || baseDamage <= 0) {
            throw new IllegalArgumentException("Unit values must be positive (cost may be zero)");
        }
        this.unitTypeId = unitTypeId;
        this.cost = cost;
        this.maxHealth = maxHealth;
        this.speedUnitsPerTick = speedUnitsPerTick;
        this.baseDamage = baseDamage;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public int getCost() {
        return cost;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public long getSpeedUnitsPerTick() {
        return speedUnitsPerTick;
    }

    public int getBaseDamage() {
        return baseDamage;
    }
}
