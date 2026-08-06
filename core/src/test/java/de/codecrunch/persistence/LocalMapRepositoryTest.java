package de.codecrunch.persistence;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Collections;

import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LocalMapRepositoryTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void savesListsAndLoadsSerializedMaps() throws Exception {
        LocalMapRepository repository = new LocalMapRepository(temporaryFolder.newFolder("maps"));
        M_Map map = new M_Map();
        map.setMapName("first");
        map.getTile(2, 3).setTileState(ME_TileState.START);
        map.deflate();

        assertTrue(repository.save(map));
        assertEquals(Collections.singletonList("first"), repository.getMapNames());

        M_Map loaded = repository.load("first");
        assertNotNull(loaded);
        loaded.inflate();
        assertEquals("first", loaded.getMapName());
        assertEquals(ME_TileState.START, loaded.getTile(2, 3).getTileState());
    }

    @Test
    public void returnsNullForAMissingMap() throws Exception {
        LocalMapRepository repository = new LocalMapRepository(temporaryFolder.newFolder("maps"));

        assertNull(repository.load("missing"));
    }
}
