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

public class PathEnemyTest {
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
        Optional<EnemyRoute> route = EnemyRoute.fromMap(map);

        assertTrue(route.isPresent());
        assertEquals(3, route.get().size());
    }

    @Test
    public void enemyInterpolatesAcrossCornersUsingUnitSpeed() {
        PathEnemy enemy = new PathEnemy(new M_SmallUnit(), route());

        enemy.update(0.25f);
        assertEquals(1.5f, enemy.getX(), 0.001f);
        assertEquals(1f, enemy.getY(), 0.001f);

        enemy.update(0.5f);
        assertEquals(2f, enemy.getX(), 0.001f);
        assertEquals(1.5f, enemy.getY(), 0.001f);
        assertFalse(enemy.isFinished());
    }

    @Test
    public void enemyStopsAtExitAfterCompletingRoute() {
        PathEnemy enemy = new PathEnemy(new M_SmallUnit(), route());

        enemy.update(10f);
        enemy.update(1f);

        assertTrue(enemy.isFinished());
        assertEquals(2f, enemy.getX(), 0.001f);
        assertEquals(2f, enemy.getY(), 0.001f);
    }

    @Test
    public void movementIsDeterministicAcrossUpdatePartitions() {
        PathEnemy singleUpdate = new PathEnemy(new M_SmallUnit(), route());
        PathEnemy splitUpdate = new PathEnemy(new M_SmallUnit(), route());

        singleUpdate.update(0.75f);
        splitUpdate.update(0.25f);
        splitUpdate.update(0.5f);

        assertEquals(singleUpdate.getX(), splitUpdate.getX(), 0.001f);
        assertEquals(singleUpdate.getY(), splitUpdate.getY(), 0.001f);
    }

    @Test
    public void routeRejectsMissingExitAndDisconnectedTiles() {
        map.getTile(2, 2).setTileState(ME_TileState.PATH_STRAIGHT);
        assertFalse(EnemyRoute.fromMap(map).isPresent());

        map.getTile(2, 2).setTileState(ME_TileState.END);
        map.setPath(Arrays.asList(map.getTile(1, 1), map.getTile(4, 1), map.getTile(2, 2)));
        assertFalse(EnemyRoute.fromMap(map).isPresent());
    }

    private EnemyRoute route() {
        return EnemyRoute.fromMap(map).get();
    }
}
