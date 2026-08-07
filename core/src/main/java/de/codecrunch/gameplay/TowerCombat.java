package de.codecrunch.gameplay;

import java.util.List;

import de.codecrunch.model.tower.MA_Tower;

public final class TowerCombat {
    public static final int DAMAGE_PER_ATTACK = 25;

    public void attackAll(List<MA_Tower> towers, List<PathUnit> units) {
        if (towers == null) return;
        for (MA_Tower tower : towers) attack(tower, units);
    }

    public PathUnit attack(MA_Tower tower, List<PathUnit> units) {
        PathUnit target = selectTarget(tower, units);
        if (target != null) target.getUnit().takeDamage(DAMAGE_PER_ATTACK);
        return target;
    }

    public PathUnit selectTarget(MA_Tower tower, List<PathUnit> units) {
        if (tower == null || units == null) return null;

        PathUnit selected = null;
        for (PathUnit unit : units) {
            if (!isInRange(tower, unit)) continue;
            if (selected == null || unit.getRouteProgress() > selected.getRouteProgress()) {
                selected = unit;
            }
        }
        return selected;
    }

    private boolean isInRange(MA_Tower tower, PathUnit unit) {
        if (unit == null || !unit.isRunning()) return false;
        float xDistance = Math.abs(tower.getX() - unit.getX());
        float yDistance = Math.abs(tower.getY() - unit.getY());
        return Math.max(xDistance, yDistance) <= tower.getRange();
    }
}
