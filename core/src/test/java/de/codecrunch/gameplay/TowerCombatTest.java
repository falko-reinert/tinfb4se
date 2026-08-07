package de.codecrunch.gameplay;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.tower.M_BigTower;
import de.codecrunch.model.tower.M_MediumTower;
import de.codecrunch.model.tower.M_SmallTower;
import de.codecrunch.model.unit.M_SmallUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TowerCombatTest {
    private TowerCombat combat;
    private UnitRoute route;

    @Before
    public void setUp() {
        combat = new TowerCombat();
        route = UnitRoute.fromMap(horizontalMap()).get();
    }

    @Test
    public void selectsFurthestRunningUnitWithinRange() {
        M_MediumTower tower = new M_MediumTower();
        tower.setPos(3, 2);
        PathUnit earlier = unitAt(0.5f);
        PathUnit later = unitAt(1.25f);
        PathUnit outOfRange = unitAt(2.25f);

        PathUnit selected = combat.selectTarget(tower,
                Arrays.asList(earlier, outOfRange, later));

        assertSame(later, selected);
    }

    @Test
    public void equalProgressKeepsStableInputOrder() {
        M_BigTower tower = new M_BigTower();
        tower.setPos(1, 2);
        PathUnit first = unitAt(0.5f);
        PathUnit second = unitAt(0.5f);

        assertSame(first, combat.selectTarget(tower, Arrays.asList(first, second)));
    }

    @Test
    public void attackUsesExistingTowerRangeAndFixedDamage() {
        M_SmallTower tower = new M_SmallTower();
        tower.setPos(2, 2);
        PathUnit unit = unitAt(0f);

        PathUnit attacked = combat.attack(tower, Collections.singletonList(unit));

        assertSame(unit, attacked);
        assertEquals(75, unit.getUnit().getCurrentLife());
    }

    @Test
    public void deadAndFinishedUnitsAreNeverTargeted() {
        M_BigTower tower = new M_BigTower();
        tower.setPos(2, 2);
        PathUnit dead = unitAt(0f);
        for (int attack = 0; attack < 4; attack++) {
            assertSame(dead, combat.attack(tower, Collections.singletonList(dead)));
        }
        PathUnit finished = unitAt(10f);

        assertEquals(0, dead.getUnit().getCurrentLife());
        assertNull(combat.attack(tower, Collections.singletonList(dead)));
        assertNull(combat.selectTarget(tower, Arrays.asList(dead, finished)));
    }

    @Test
    public void eachDefendingTowerAttacksOncePerCombatUpdate() {
        M_SmallTower first = new M_SmallTower();
        first.setPos(2, 2);
        M_MediumTower second = new M_MediumTower();
        second.setPos(3, 2);
        PathUnit unit = unitAt(0f);

        combat.attackAll(Arrays.asList(first, second), Collections.singletonList(unit));

        assertEquals(50, unit.getUnit().getCurrentLife());
    }

    @Test
    public void laterTowerRetargetsWhenEarlierTowerDefeatsTheLeadingUnit() {
        M_BigTower first = new M_BigTower();
        first.setPos(2, 2);
        M_BigTower second = new M_BigTower();
        second.setPos(2, 2);
        PathUnit following = unitAt(0f);
        PathUnit leading = unitAt(0.5f);
        leading.getUnit().setCurrentLife(TowerCombat.DAMAGE_PER_ATTACK);

        combat.attackAll(Arrays.asList(first, second), Arrays.asList(following, leading));

        assertEquals(0, leading.getUnit().getCurrentLife());
        assertEquals(75, following.getUnit().getCurrentLife());
    }

    private PathUnit unitAt(float seconds) {
        PathUnit unit = new PathUnit(new M_SmallUnit(), route);
        unit.update(seconds);
        return unit;
    }

    private M_Map horizontalMap() {
        M_Map map = new M_Map();
        for (int x = 1; x <= 6; x++) {
            ME_TileState state = x == 1 ? ME_TileState.START
                    : x == 6 ? ME_TileState.END : ME_TileState.PATH_STRAIGHT;
            map.getTile(x, 1).setTileState(state);
        }
        map.setPath(Arrays.asList(map.getTile(1, 1), map.getTile(2, 1),
                map.getTile(3, 1), map.getTile(4, 1), map.getTile(5, 1), map.getTile(6, 1)));
        return map;
    }
}
