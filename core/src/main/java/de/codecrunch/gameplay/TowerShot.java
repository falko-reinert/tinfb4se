package de.codecrunch.gameplay;

import de.codecrunch.model.tower.MA_Tower;

public final class TowerShot {
    private final MA_Tower tower;
    private final PathUnit target;

    public TowerShot(MA_Tower tower, PathUnit target) {
        if (tower == null || target == null) throw new IllegalArgumentException("Tower and target are required");
        this.tower = tower;
        this.target = target;
    }

    public MA_Tower getTower() {
        return tower;
    }

    public PathUnit getTarget() {
        return target;
    }
}
