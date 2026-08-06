package de.codecrunch.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.M_Path;
import de.codecrunch.model.M_Tile;

public class EditorRules {
    private final M_Map map;
    private final M_Path<M_Tile> path = new M_Path<>();

    public EditorRules(M_Map map) {
        this.map = map == null ? new M_Map() : map;
        if (map != null) path.addFromList(map.getPath());
    }

    public M_Map getMap() {
        return map;
    }

    public List<M_Tile> addToPath(M_Tile tile) {
        Set<M_Tile> changedTiles = new LinkedHashSet<>();
        if (tile.getTileState() != ME_TileState.EMPTY) return Collections.emptyList();

        if (path.isEmpty()) {
            path.addFront(tile);
            setState(tile, ME_TileState.START, changedTiles);
            return new ArrayList<>(changedTiles);
        }

        M_Tile head = path.head().get();
        M_Tile tail = path.tail().get();
        if (areNeighbours(head, tile)) {
            path.addFront(tile);
            setState(tile, ME_TileState.END, changedTiles);
            update(path.head(), changedTiles);
            update(path.head().prev(), changedTiles);
        } else if (areNeighbours(tail, tile)) {
            path.addBack(tile);
            setState(tile, ME_TileState.START, changedTiles);
            update(path.tail(), changedTiles);
            update(path.tail().next(), changedTiles);
        }
        return new ArrayList<>(changedTiles);
    }

    public List<M_Tile> removeFromPath(M_Tile tile) {
        Set<M_Tile> changedTiles = new LinkedHashSet<>();
        if (tile.getTileState() == ME_TileState.EMPTY || path.isEmpty()) {
            return Collections.emptyList();
        }

        if (tile == path.head().get()) {
            setState(path.head().get(), ME_TileState.EMPTY, changedTiles);
            M_Path<M_Tile>.Node newHead = path.head().prev();
            path.removeFront();
            if (newHead != null) setState(newHead.get(), ME_TileState.END, changedTiles);
            if (newHead == path.tail() && newHead != null) {
                setState(newHead.get(), ME_TileState.START, changedTiles);
            }
        } else if (tile == path.tail().get()) {
            setState(path.tail().get(), ME_TileState.EMPTY, changedTiles);
            M_Path<M_Tile>.Node newTail = path.tail().next();
            path.removeBack();
            if (newTail != null) setState(newTail.get(), ME_TileState.START, changedTiles);
        }
        return new ArrayList<>(changedTiles);
    }

    public boolean prepareMapForSave(String levelName) {
        if (levelName.length() < 1) return false;

        map.setMapName(levelName);
        map.setPath(path.addToList(new ArrayList<M_Tile>()));
        map.deflate();
        return true;
    }

    private boolean areNeighbours(M_Tile first, M_Tile second) {
        return Math.abs(first.x_pos - second.x_pos) <= 1 && first.y_pos == second.y_pos
                || Math.abs(first.y_pos - second.y_pos) <= 1 && first.x_pos == second.x_pos;
    }

    private void update(M_Path<M_Tile>.Node node, Set<M_Tile> changedTiles) {
        M_Tile neighbour;
        if (!node.hasPrev() && !node.hasNext()) return;

        if (node.hasPrev()) neighbour = node.prev().get();
        else neighbour = node.next().get();

        int rotation = (90 * (2 - (neighbour.y_pos - node.get().y_pos))
                - (neighbour.x_pos < node.get().x_pos ? 180 : 0)
                + (!node.hasPrev() ? 180 : 0)) % 360;
        node.get().setTileRotation(rotation);
        changedTiles.add(node.get());

        if (node.hasPrev() && node.hasNext()) {
            if (node.prev().get().x_pos == node.next().get().x_pos
                    || node.prev().get().y_pos == node.next().get().y_pos) {
                setState(node.get(), ME_TileState.PATH_STRAIGHT, changedTiles);
                return;
            }

            if (node.prev().get().x_pos == node.get().x_pos) {
                if (node.prev().get().y_pos < node.get().y_pos) {
                    if (node.next().get().x_pos < node.get().x_pos) {
                        setState(node.get(), ME_TileState.PATH_RIGHT, changedTiles);
                    } else {
                        setState(node.get(), ME_TileState.PATH_LEFT, changedTiles);
                    }
                } else if (node.next().get().x_pos > node.get().x_pos) {
                    setState(node.get(), ME_TileState.PATH_RIGHT, changedTiles);
                } else {
                    setState(node.get(), ME_TileState.PATH_LEFT, changedTiles);
                }
            } else if (node.prev().get().x_pos < node.get().x_pos) {
                if (node.next().get().y_pos > node.get().y_pos) {
                    setState(node.get(), ME_TileState.PATH_RIGHT, changedTiles);
                } else {
                    setState(node.get(), ME_TileState.PATH_LEFT, changedTiles);
                }
            } else if (node.next().get().y_pos < node.get().y_pos) {
                setState(node.get(), ME_TileState.PATH_RIGHT, changedTiles);
            } else {
                setState(node.get(), ME_TileState.PATH_LEFT, changedTiles);
            }
        }
    }

    private void setState(M_Tile tile, ME_TileState state, Set<M_Tile> changedTiles) {
        tile.setTileState(state);
        changedTiles.add(tile);
    }
}
