package de.codecrunch.gameplay.simulation.spec;

/** Immutable combat values for one defending tower. */
public final class TowerSpec {
    private final String towerId;
    private final long routePositionUnits;
    private final long rangeUnits;
    private final int damage;
    private final int cooldownTicks;
    private final TargetRule targetRule;

    public TowerSpec(String towerId, long routePositionUnits, long rangeUnits,
                     int damage, int cooldownTicks, TargetRule targetRule) {
        if (towerId == null || towerId.trim().isEmpty()) {
            throw new IllegalArgumentException("towerId is required");
        }
        if (routePositionUnits < 0 || rangeUnits < 0 || damage <= 0 || cooldownTicks <= 0) {
            throw new IllegalArgumentException("Tower values are outside their valid range");
        }
        if (targetRule == null) {
            throw new IllegalArgumentException("targetRule is required");
        }
        this.towerId = towerId;
        this.routePositionUnits = routePositionUnits;
        this.rangeUnits = rangeUnits;
        this.damage = damage;
        this.cooldownTicks = cooldownTicks;
        this.targetRule = targetRule;
    }

    public String getTowerId() {
        return towerId;
    }

    public long getRoutePositionUnits() {
        return routePositionUnits;
    }

    public long getRangeUnits() {
        return rangeUnits;
    }

    public int getDamage() {
        return damage;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public TargetRule getTargetRule() {
        return targetRule;
    }
}
