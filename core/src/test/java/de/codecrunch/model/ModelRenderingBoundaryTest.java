package de.codecrunch.model;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;

import de.codecrunch.model.tower.MA_Tower;
import de.codecrunch.model.tower.M_BigTower;
import de.codecrunch.model.tower.M_MediumTower;
import de.codecrunch.model.tower.M_SmallTower;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertArrayEquals;

public class ModelRenderingBoundaryTest {
    private static final String LEGACY_TILE =
            "rO0ABXNyABpkZS5jb2RlY3J1bmNoLm1vZGVsLk1fVGlsZQ57X9y00AuYAgAESQAM"
                    + "dGlsZVJvdGF0aW9uSQAQdGlsZVN0YXRlT3JkaW5hbEkABXhfcG9zSQAFeV9wb3N4"
                    + "cAAAAFoAAAABAAAAAgAAAAM=";

    @Test
    public void tileAndTowerModelsExposeNoRenderingTypes() {
        Class<?>[] modelTypes = {
                M_Tile.class,
                ME_TileState.class,
                MA_Tower.class,
                M_SmallTower.class,
                M_MediumTower.class,
                M_BigTower.class
        };

        assertEquals(Object.class, M_Tile.class.getSuperclass());
        for (Class<?> modelType : modelTypes) assertNoLibGdxTypes(modelType);
    }

    @Test
    public void readsTilesWrittenBeforeScene2DWasRemoved() throws Exception {
        ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(Base64.getDecoder().decode(LEGACY_TILE)));

        M_Tile tile = (M_Tile) input.readObject();

        assertEquals(1043533140197378968L,
                ObjectStreamClass.lookup(M_Tile.class).getSerialVersionUID());
        assertEquals(2, tile.x_pos);
        assertEquals(3, tile.y_pos);
        assertEquals(90, tile.getTileRotation());
        assertNull(tile.getTileState());
        tile.inflate();
        assertEquals(ME_TileState.START, tile.getTileState());
    }

    @Test
    public void towerPositionRemainsGameplayState() {
        MA_Tower tower = new M_SmallTower();

        tower.setPos(4, 7);

        assertEquals(4, tower.getX());
        assertEquals(7, tower.getY());
    }

    @Test
    public void persistedTileStateOrderingRemainsStable() {
        assertArrayEquals(new String[] {
                "EMPTY", "START", "END", "OCCUPIED", "PATH_STRAIGHT",
                "PATH_LEFT", "PATH_RIGHT", "PATH_CROSS", "PATH_SPLIT_RIGHT",
                "PATH_SPLIT_LEFT"
        }, names(ME_TileState.values()));
    }

    private void assertNoLibGdxTypes(Class<?> type) {
        for (Field field : type.getDeclaredFields()) assertNotLibGdx(field.getType());
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            for (Class<?> parameter : constructor.getParameterTypes()) assertNotLibGdx(parameter);
        }
        for (Method method : type.getDeclaredMethods()) {
            assertNotLibGdx(method.getReturnType());
            for (Class<?> parameter : method.getParameterTypes()) assertNotLibGdx(parameter);
        }
    }

    private void assertNotLibGdx(Class<?> type) {
        assertFalse(type.getName(), type.getName().startsWith("com.badlogic.gdx"));
    }

    private String[] names(ME_TileState[] states) {
        String[] names = new String[states.length];
        for (int i = 0; i < states.length; i++) names[i] = states[i].name();
        return names;
    }
}
