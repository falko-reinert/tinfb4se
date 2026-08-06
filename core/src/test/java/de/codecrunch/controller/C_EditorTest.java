package de.codecrunch.controller;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import de.codecrunch.ScreenId;
import de.codecrunch.ScreenNavigator;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;
import de.codecrunch.persistence.MapRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class C_EditorTest {
    private RecordingNavigator navigator;
    private RecordingMapRepository repository;
    private C_Editor editor;

    @Before
    public void setUp() {
        navigator = new RecordingNavigator();
        repository = new RecordingMapRepository();
        editor = new C_Editor(navigator, null, repository);
    }

    @Test
    public void savePersistsPreparedMapThenReturnsToLevelSelection() {
        M_Map map = editor.getMap();
        editor.addToPath(map.getTile(2, 2));
        editor.addToPath(map.getTile(3, 2));

        editor.saveLevel("level");

        assertSame(map, repository.savedMap);
        assertEquals("level", repository.savedMap.getMapName());
        assertEquals(2, repository.savedMap.getPath().size());
        assertEquals(ME_TileState.START, repository.savedMap.getPath().get(0).getTileState());
        assertEquals(ME_TileState.END, repository.savedMap.getPath().get(1).getTileState());
        assertEquals(ScreenId.EDITOR_LEVEL_SELECT, navigator.screenId);
    }

    @Test
    public void emptyNameDoesNotPersistOrNavigate() {
        editor.saveLevel("");

        assertNull(repository.savedMap);
        assertNull(navigator.screenId);
    }

    private static class RecordingNavigator implements ScreenNavigator {
        private ScreenId screenId;

        @Override
        public void navigateTo(ScreenId screenId) {
            this.screenId = screenId;
        }
    }

    private static class RecordingMapRepository implements MapRepository {
        private M_Map savedMap;

        @Override
        public List<String> getMapNames() {
            return Collections.emptyList();
        }

        @Override
        public M_Map load(String name) {
            return null;
        }

        @Override
        public boolean save(M_Map map) {
            savedMap = map;
            return true;
        }
    }
}
