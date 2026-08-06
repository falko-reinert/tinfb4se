package de.codecrunch;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Timer.Task;

import de.codecrunch.controller.C_Game;
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

    @Test
    public void screensDeclareTheirInputProcessor() throws Exception {
        Method defaultInput = VA_Screen.class.getDeclaredMethod("getInputProcessor");
        Method gameInput = V_Game.class.getDeclaredMethod("getInputProcessor");
        Field gameInputField = V_Game.class.getDeclaredField("inputProcessor");

        assertTrue(InputProcessor.class.isAssignableFrom(defaultInput.getReturnType()));
        assertTrue(InputProcessor.class.isAssignableFrom(gameInput.getReturnType()));
        assertTrue(InputProcessor.class.isAssignableFrom(gameInputField.getType()));
        assertTrue(Modifier.isFinal(gameInputField.getModifiers()));
    }

    @Test
    public void lifecycleOwnersExposeDisposal() throws Exception {
        assertTrue(void.class.equals(TowerAttackGame.class.getDeclaredMethod("dispose").getReturnType()));
        assertTrue(void.class.equals(C_Game.class.getDeclaredMethod("dispose").getReturnType()));
        assertTrue(void.class.equals(Model3DFactory.class.getDeclaredMethod("dispose").getReturnType()));
        Field taskField = C_Game.class.getDeclaredField("computerTask");
        assertTrue(Task.class.equals(taskField.getType()));
    }
}
