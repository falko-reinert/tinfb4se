package de.codecrunch.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import de.codecrunch.persistence.MapRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class M_LevelSelectTest {
    @Test
    public void obtainsLevelNamesFromTheRepository() {
        FakeMapRepository repository = new FakeMapRepository();
        repository.names = Arrays.asList("one", "two");

        M_LevelSelect levelSelect = new M_LevelSelect(repository);

        assertEquals(repository.names, levelSelect.getLevels());
    }

    @Test
    public void inflatesLoadedMapsInTheGameModel() {
        FakeMapRepository repository = new FakeMapRepository();
        repository.map = new M_Map();
        repository.map.getTile(1, 1).setTileState(ME_TileState.START);
        repository.map.deflate();
        repository.map.getTile(1, 1).setTileState(ME_TileState.EMPTY);

        M_Map loaded = new M_LevelSelect(repository).loadMap("one");

        assertEquals("one", repository.loadedName);
        assertEquals(ME_TileState.START, loaded.getTile(1, 1).getTileState());
    }

    @Test
    public void preservesMissingMapBehavior() {
        assertNull(new M_LevelSelect(new FakeMapRepository()).loadMap("missing"));
    }

    private static class FakeMapRepository implements MapRepository {
        private List<String> names;
        private M_Map map;
        private String loadedName;

        @Override
        public List<String> getMapNames() {
            return names;
        }

        @Override
        public M_Map load(String name) {
            loadedName = name;
            return map;
        }

        @Override
        public boolean save(M_Map map) {
            this.map = map;
            return true;
        }
    }
}
