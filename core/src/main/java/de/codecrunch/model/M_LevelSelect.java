package de.codecrunch.model;

import java.util.Collections;
import java.util.List;

import de.codecrunch.persistence.MapRepository;

public class M_LevelSelect {
    private final int displayCount = 4;
    private int index = 0;
    private List<String> levels = Collections.emptyList();
    private final MapRepository mapRepository;

    public M_LevelSelect(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public int getDisplayCount() {
        return displayCount;
    }

    public boolean changeIndex(int delta) {
        if (index + delta >= 0 && index + delta + displayCount <= levels.size()) {
            index += delta;
            return true;
        }
        return false;
    }

    public List<String> getLevels() {
        load();
        return levels.subList(index, Math.min(displayCount + index + 1, levels.size()));
    }

    public void load() {
        levels = mapRepository.getMapNames();
    }

    public M_Map loadMap(String name) {
        M_Map map = mapRepository.load(name);
        if (map == null) return null;
        map.inflate();
        return map;
    }
}
