package de.codecrunch.gameplay;

import java.util.ArrayList;
import java.util.List;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.M_Tile;

public final class CombatDemoMap {
    private CombatDemoMap() {
    }

    public static M_Map create() {
        M_Map map = new M_Map();
        map.setMapName("Combat demo");
        List<M_Tile> path = new ArrayList<>();
        for (int x = 2; x <= 17; x++) {
            ME_TileState state = x == 2 ? ME_TileState.START
                    : x == 17 ? ME_TileState.END : ME_TileState.PATH_STRAIGHT;
            M_Tile tile = map.getTile(x, 4);
            tile.setTileState(state);
            path.add(tile);
        }
        map.setPath(path);
        return map;
    }
}
