package de.codecrunch;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import de.codecrunch.view.VA_LevelSelect;
import de.codecrunch.view.VA_Screen;
import de.codecrunch.view.V_Editor;
import de.codecrunch.view.V_EditorLevelSelect;
import de.codecrunch.view.V_Game;
import de.codecrunch.view.V_GameLevelSelect;
import de.codecrunch.view.V_Menu;
import de.codecrunch.view.V_Settings;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class TowerAttackGameTest {

    @Test
    public void towerAttackGameProvidesScreenNavigation() {
        assertTrue(ScreenNavigator.class.isAssignableFrom(TowerAttackGame.class));
    }

    @Test
    public void screensDoNotDependOnTowerAttackGame() {
        Class<?>[] screenTypes = {
                VA_Screen.class,
                VA_LevelSelect.class,
                V_Editor.class,
                V_EditorLevelSelect.class,
                V_Game.class,
                V_GameLevelSelect.class,
                V_Menu.class,
                V_Settings.class
        };

        for (Class<?> screenType : screenTypes) {
            for (Field field : screenType.getDeclaredFields()) {
                assertNotEquals(screenType.getSimpleName(), TowerAttackGame.class, field.getType());
            }
            for (Constructor<?> constructor : screenType.getDeclaredConstructors()) {
                for (Class<?> parameterType : constructor.getParameterTypes()) {
                    assertNotEquals(screenType.getSimpleName(), TowerAttackGame.class, parameterType);
                }
            }
        }
    }
}
