package de.codecrunch.gameplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.M_Tile;

public final class EnemyRoute {
    private final List<RoutePoint> points;

    private EnemyRoute(List<RoutePoint> points) {
        this.points = Collections.unmodifiableList(points);
    }

    public static Optional<EnemyRoute> fromMap(M_Map map) {
        if (map == null || map.getPath() == null || map.getPath().size() < 2) {
            return Optional.empty();
        }

        List<M_Tile> path = map.getPath();
        M_Tile start = path.get(0);
        M_Tile end = path.get(path.size() - 1);
        if (start == null || end == null || start.getTileState() != ME_TileState.START
                || end.getTileState() != ME_TileState.END) {
            return Optional.empty();
        }

        List<RoutePoint> points = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            M_Tile tile = path.get(i);
            if (tile == null || tile.getTileState() == null
                    || tile.getTileState().getGroup() != ME_TileState.ME_TileStateGroup.PATH) {
                return Optional.empty();
            }
            if (i > 0 && !areNeighbours(path.get(i - 1), tile)) {
                return Optional.empty();
            }
            points.add(new RoutePoint(tile.x_pos, tile.y_pos));
        }
        return Optional.of(new EnemyRoute(points));
    }

    public int size() {
        return points.size();
    }

    float getX(int index) {
        return points.get(index).x;
    }

    float getY(int index) {
        return points.get(index).y;
    }

    private static boolean areNeighbours(M_Tile first, M_Tile second) {
        return Math.abs(first.x_pos - second.x_pos)
                + Math.abs(first.y_pos - second.y_pos) == 1;
    }

    private static final class RoutePoint {
        private final float x;
        private final float y;

        private RoutePoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
