package de.codecrunch.gameplay.simulation.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Immutable input for one deterministic combat scenario. */
public final class ScenarioSpec {
    public static final int TICKS_PER_SECOND = 60;
    public static final long ROUTE_UNITS_PER_PROGRESS = 6000L;

    private final String scenarioId;
    private final long routeLengthUnits;
    private final int baseIntegrity;
    private final int budget;
    private final int maxDeployments;
    private final int maxSpawnTick;
    private final int spawnGridTicks;
    private final int timeLimitTicks;
    private final List<UnitSpec> unitSpecs;
    private final List<TowerSpec> towerSpecs;
    private final Map<String, UnitSpec> unitSpecsById;

    public ScenarioSpec(String scenarioId, long routeLengthUnits, int baseIntegrity,
                        int budget, int maxDeployments, int maxSpawnTick,
                        int spawnGridTicks, int timeLimitTicks,
                        List<UnitSpec> unitSpecs, List<TowerSpec> towerSpecs) {
        if (scenarioId == null || scenarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("scenarioId is required");
        }
        if (routeLengthUnits <= 0 || baseIntegrity <= 0 || budget < 0
                || maxDeployments <= 0 || maxSpawnTick < 0 || spawnGridTicks <= 0
                || timeLimitTicks <= 0) {
            throw new IllegalArgumentException("Scenario values are outside their valid range");
        }
        if (unitSpecs == null || unitSpecs.isEmpty() || towerSpecs == null) {
            throw new IllegalArgumentException("Unit specs and tower specs are required");
        }

        TreeMap<String, UnitSpec> units = new TreeMap<>();
        for (UnitSpec unitSpec : unitSpecs) {
            if (unitSpec == null || units.put(unitSpec.getUnitTypeId(), unitSpec) != null) {
                throw new IllegalArgumentException("Unit IDs must be non-null and unique");
            }
        }
        TreeMap<String, TowerSpec> towers = new TreeMap<>();
        for (TowerSpec towerSpec : towerSpecs) {
            if (towerSpec == null || towers.put(towerSpec.getTowerId(), towerSpec) != null) {
                throw new IllegalArgumentException("Tower IDs must be non-null and unique");
            }
            if (towerSpec.getRoutePositionUnits() > routeLengthUnits) {
                throw new IllegalArgumentException("Tower position must lie on the route");
            }
        }

        this.scenarioId = scenarioId;
        this.routeLengthUnits = routeLengthUnits;
        this.baseIntegrity = baseIntegrity;
        this.budget = budget;
        this.maxDeployments = maxDeployments;
        this.maxSpawnTick = maxSpawnTick;
        this.spawnGridTicks = spawnGridTicks;
        this.timeLimitTicks = timeLimitTicks;
        this.unitSpecsById = Collections.unmodifiableMap(units);
        this.unitSpecs = Collections.unmodifiableList(new ArrayList<>(units.values()));
        this.towerSpecs = Collections.unmodifiableList(new ArrayList<>(towers.values()));
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public long getRouteLengthUnits() {
        return routeLengthUnits;
    }

    public int getBaseIntegrity() {
        return baseIntegrity;
    }

    public int getBudget() {
        return budget;
    }

    public int getMaxDeployments() {
        return maxDeployments;
    }

    public int getMaxSpawnTick() {
        return maxSpawnTick;
    }

    public int getSpawnGridTicks() {
        return spawnGridTicks;
    }

    public int getTimeLimitTicks() {
        return timeLimitTicks;
    }

    public List<UnitSpec> getUnitSpecs() {
        return unitSpecs;
    }

    public UnitSpec getUnitSpec(String unitTypeId) {
        return unitSpecsById.get(unitTypeId);
    }

    public List<TowerSpec> getTowerSpecs() {
        return towerSpecs;
    }
}
