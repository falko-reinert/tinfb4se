package de.codecrunch.editor;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.M_Tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EditorRulesTest {
    private M_Map map;
    private EditorRules rules;

    @Before
    public void setUp() {
        rules = new EditorRules(null);
        map = rules.getMap();
    }

    @Test
    public void firstPlacementCreatesTheStartTile() {
        M_Tile first = tile(2, 2);

        List<M_Tile> changed = rules.addToPath(first);

        assertEquals(ME_TileState.START, first.getTileState());
        assertEquals(1, changed.size());
        assertTrue(changed.contains(first));
    }

    @Test
    public void straightPathMaintainsStartEndAndMiddleStates() {
        M_Tile start = tile(2, 2);
        M_Tile middle = tile(3, 2);
        M_Tile end = tile(4, 2);

        rules.addToPath(start);
        rules.addToPath(middle);
        rules.addToPath(end);

        assertEquals(ME_TileState.START, start.getTileState());
        assertEquals(ME_TileState.PATH_STRAIGHT, middle.getTileState());
        assertEquals(ME_TileState.END, end.getTileState());
        assertEquals(0, start.getTileRotation());
        assertEquals(0, middle.getTileRotation());
        assertEquals(0, end.getTileRotation());
    }

    @Test
    public void cornerPlacementPreservesEveryTurnOrientation() {
        assertCorner(2, 2, 3, 2, 3, 3, ME_TileState.PATH_RIGHT, 0);
        assertCorner(2, 2, 3, 2, 3, 1, ME_TileState.PATH_LEFT, 0);
        assertCorner(4, 2, 3, 2, 3, 3, ME_TileState.PATH_LEFT, 180);
        assertCorner(4, 2, 3, 2, 3, 1, ME_TileState.PATH_RIGHT, 180);
        assertCorner(3, 1, 3, 2, 4, 2, ME_TileState.PATH_LEFT, 270);
        assertCorner(3, 1, 3, 2, 2, 2, ME_TileState.PATH_RIGHT, 270);
        assertCorner(3, 3, 3, 2, 4, 2, ME_TileState.PATH_RIGHT, 90);
        assertCorner(3, 3, 3, 2, 2, 2, ME_TileState.PATH_LEFT, 90);
    }

    @Test
    public void placementRejectsDiagonalAndNonEndpointTiles() {
        M_Tile start = tile(2, 2);
        M_Tile middle = tile(3, 2);
        M_Tile end = tile(4, 2);
        M_Tile diagonal = tile(5, 3);
        M_Tile besideMiddle = tile(3, 3);

        rules.addToPath(start);
        rules.addToPath(middle);
        rules.addToPath(end);

        assertTrue(rules.addToPath(diagonal).isEmpty());
        assertTrue(rules.addToPath(besideMiddle).isEmpty());
        assertEquals(ME_TileState.EMPTY, diagonal.getTileState());
        assertEquals(ME_TileState.EMPTY, besideMiddle.getTileState());
    }

    @Test
    public void removalOnlyAcceptsPathEndpoints() {
        M_Tile start = tile(2, 2);
        M_Tile middle = tile(3, 2);
        M_Tile end = tile(4, 2);
        rules.addToPath(start);
        rules.addToPath(middle);
        rules.addToPath(end);

        assertTrue(rules.removeFromPath(middle).isEmpty());
        assertEquals(ME_TileState.PATH_STRAIGHT, middle.getTileState());

        List<M_Tile> changed = rules.removeFromPath(end);
        assertEquals(ME_TileState.EMPTY, end.getTileState());
        assertEquals(ME_TileState.END, middle.getTileState());
        assertTrue(changed.contains(end));
        assertTrue(changed.contains(middle));
    }

    @Test
    public void pathCanExtendAndRemoveAtTheStartEndpoint() {
        M_Tile middle = tile(2, 2);
        M_Tile end = tile(3, 2);
        M_Tile start = tile(1, 2);
        rules.addToPath(middle);
        rules.addToPath(end);

        rules.addToPath(start);

        assertEquals(ME_TileState.START, start.getTileState());
        assertEquals(ME_TileState.PATH_STRAIGHT, middle.getTileState());
        assertEquals(ME_TileState.END, end.getTileState());

        rules.removeFromPath(start);
        assertEquals(ME_TileState.EMPTY, start.getTileState());
        assertEquals(ME_TileState.START, middle.getTileState());
    }

    @Test
    public void savePreparationOwnsNamePathOrderAndDeflation() {
        M_Tile start = tile(2, 2);
        M_Tile end = tile(3, 2);
        rules.addToPath(start);
        rules.addToPath(end);

        assertFalse(rules.prepareMapForSave(""));
        assertTrue(rules.prepareMapForSave("level"));
        assertEquals("level", map.getMapName());
        assertEquals(start, map.getPath().get(0));
        assertEquals(end, map.getPath().get(1));

        start.setTileState(ME_TileState.EMPTY);
        map.inflate();
        assertEquals(ME_TileState.START, start.getTileState());
    }

    @Test
    public void loadedMapPathCanContinueAtItsEndpoint() {
        M_Tile start = tile(2, 2);
        M_Tile previousEnd = tile(3, 2);
        rules.addToPath(start);
        rules.addToPath(previousEnd);
        rules.prepareMapForSave("level");

        EditorRules loadedRules = new EditorRules(map);
        M_Tile newEnd = tile(4, 2);
        loadedRules.addToPath(newEnd);

        assertEquals(ME_TileState.START, start.getTileState());
        assertEquals(ME_TileState.PATH_STRAIGHT, previousEnd.getTileState());
        assertEquals(ME_TileState.END, newEnd.getTileState());
    }

    private M_Tile tile(int x, int y) {
        return map.getTile(x, y);
    }

    private void assertCorner(int startX, int startY, int cornerX, int cornerY,
                              int endX, int endY, ME_TileState state, int rotation) {
        EditorRules cornerRules = new EditorRules(null);
        M_Map cornerMap = cornerRules.getMap();
        M_Tile corner = cornerMap.getTile(cornerX, cornerY);

        cornerRules.addToPath(cornerMap.getTile(startX, startY));
        cornerRules.addToPath(corner);
        cornerRules.addToPath(cornerMap.getTile(endX, endY));

        assertEquals(state, corner.getTileState());
        assertEquals(rotation, corner.getTileRotation());
    }
}
