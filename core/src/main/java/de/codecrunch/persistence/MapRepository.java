package de.codecrunch.persistence;

import java.util.List;

import de.codecrunch.model.M_Map;

public interface MapRepository {
    List<String> getMapNames();

    M_Map load(String name);

    boolean save(M_Map map);
}
