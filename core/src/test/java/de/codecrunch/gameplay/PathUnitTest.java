package de.codecrunch.gameplay;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;

import de.codecrunch.editor.EditorRules;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.unit.M_SmallUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathUnitTest {
    private M_Map map;

    @Before
    public void setUp() {
        EditorRules rules = new EditorRules(null);
        map = rules.getMap();
        rules.addToPath(map.getTile(1, 1));
        rules.addToPath(map.getTile(2, 1));
        rules.addToPath(map.getTile(2, 2));
        rules.prepareMapForSave("slice");
    }

    @Test
    public void routeUsesSavedStartToEndOrder() {
        Optional<UnitRoute> route = UnitRoute.fromMap(map);

        assertTrue(route.isPresent());
        assertEquals(3, route.get().size());
    }

    @Test
    public void unitInterpolatesAcrossCornersUsingUnitSpeed() {
        PathUnit unit = new PathUnit(new M_SmallUnit(), route());

        unit.update(0.25f);
        assertEquals(1.5f, unit.getX(), 0.001f);
        assertEquals(1f, unit.getY(), 0.001f);

        unit.update(0.5f);
        assertEquals(2f, unit.getX(), 0.001f);
        assertEquals(1.5f, unit.getY(), 0.001f);
        assertFalse(unit.isFinished());
    }

    @Test
    public void unitStopsAtExitAfterCompletingRoute() {
        PathUnit unit = new PathUnit(new M_SmallUnit(), route());

        unit.update(10f);
        unit.update(1f);

        assertTrue(unit.isFinished());
        assertFalse(unit.isRunning());
        assertEquals(2f, unit.getX(), 0.001f);
        assertEquals(2f, unit.getY(), 0.001f);
    }

    @Test
    public void deadUnitIsNotRunningAndStopsMoving() {
        PathUnit unit = new PathUnit(new M_SmallUnit(), route());
        unit.getUnit().takeDamage(unit.getUnit().getMaxLife());

        unit.update(1f);

        assertFalse(unit.isRunning());
        assertEquals(1f, unit.getX(), 0.001f);
        assertEquals(1f, unit.getY(), 0.001f);
        assertEquals(0f, unit.getRouteProgress(), 0.001f);
    }

    @Test
    public void movementIsDeterministicAcrossUpdatePartitions() {
        PathUnit singleUpdate = new PathUnit(new M_SmallUnit(), route());
        PathUnit splitUpdate = new PathUnit(new M_SmallUnit(), route());

        singleUpdate.update(0.75f);
        splitUpdate.update(0.25f);
        splitUpdate.update(0.5f);

        assertEquals(singleUpdate.getX(), splitUpdate.getX(), 0.001f);
        assertEquals(singleUpdate.getY(), splitUpdate.getY(), 0.001f);
    }

    @Test
    public void routeRejectsMissingExitAndDisconnectedTiles() {
        map.getTile(2, 2).setTileState(ME_TileState.PATH_STRAIGHT);
        assertFalse(UnitRoute.fromMap(map).isPresent());

        map.getTile(2, 2).setTileState(ME_TileState.END);
        map.setPath(Arrays.asList(map.getTile(1, 1), map.getTile(4, 1), map.getTile(2, 2)));
        assertFalse(UnitRoute.fromMap(map).isPresent());
    }

    private UnitRoute route() {
        return UnitRoute.fromMap(map).get();
    }
}
