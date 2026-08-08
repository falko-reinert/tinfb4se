package de.codecrunch.gameplay;

import org.junit.Test;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;

import static org.junit.Assert.assertEquals;

public class CombatDemoMapTest {
    @Test
    public void createsLongStraightRouteForReliableCombatTesting() {
        M_Map map = CombatDemoMap.create();

        assertEquals("Combat demo", map.getMapName());
        assertEquals(16, map.getPath().size());
        assertEquals(ME_TileState.START, map.getTile(2, 4).getTileState());
        assertEquals(ME_TileState.END, map.getTile(17, 4).getTileState());
    }
}
