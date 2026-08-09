package de.codecrunch.gameplay.planning;

/** Immutable presentation state for one deployment slot. */
public final class PlanningSlotView {
    private final int slotIndex;
    private final String unitTypeId;
    private final int spawnTick;
    private final int cost;

    PlanningSlotView(int slotIndex, String unitTypeId, int spawnTick, int cost) {
        this.slotIndex = slotIndex;
        this.unitTypeId = unitTypeId;
        this.spawnTick = spawnTick;
        this.cost = cost;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public String getUnitTypeId() {
        return unitTypeId;
    }

    public int getSpawnTick() {
        return spawnTick;
    }

    public int getCost() {
        return cost;
    }

    public boolean isEmpty() {
        return unitTypeId == null;
    }
}
