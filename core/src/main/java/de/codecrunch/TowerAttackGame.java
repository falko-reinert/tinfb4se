package de.codecrunch;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import de.codecrunch.controller.C_Editor;
import de.codecrunch.controller.C_Game;
import de.codecrunch.model.M_Map;
import de.codecrunch.view.VA_Screen;
import de.codecrunch.view.V_Editor;
import de.codecrunch.view.V_EditorLevelSelect;
import de.codecrunch.view.V_Game;
import de.codecrunch.view.V_GameLevelSelect;
import de.codecrunch.view.V_Menu;
import de.codecrunch.view.V_Settings;

public class TowerAttackGame extends Game implements ScreenNavigator {

    private C_Game game;
    private C_Editor editor;

    private VA_Screen currentScreen;
    private V_Game gameScreen;
    private V_GameLevelSelect levelSelectScreen;
    private V_Menu menuScreen;
    private V_Settings settingsScreen;
    private V_Editor editorScreen;
    private V_EditorLevelSelect editorLevelSelectScreen;


    @Override
    public void create() {
        navigateTo(ScreenId.MENU);
    }

    @Override
    public void navigateTo(ScreenId screenId) {
         modifyScreen(screenId, true);
    }

    private void setupScreen(ScreenId screenId) {
        modifyScreen(screenId, false);
    }

    private void modifyScreen(ScreenId screenId, boolean changeTo) {
        switch (screenId) {
            case GAME:
                if (gameScreen == null) gameScreen = new V_Game();
                currentScreen = gameScreen;
                break;
            case GAME_LEVEL_SELECT:
                if (levelSelectScreen == null)
                    levelSelectScreen = new V_GameLevelSelect(this, this::startGame);
                currentScreen = levelSelectScreen;
                break;
            case MENU:
                if (menuScreen == null) menuScreen = new V_Menu(this);
                currentScreen = menuScreen;
                break;
            case SETTINGS:
                if (settingsScreen == null) settingsScreen = new V_Settings(this);
                currentScreen = settingsScreen;
                break;
            case EDITOR:
                if (editorScreen == null) editorScreen = new V_Editor(this);
                currentScreen = editorScreen;
                break;
            case EDITOR_LEVEL_SELECT:
                if (editorLevelSelectScreen == null)
                    editorLevelSelectScreen = new V_EditorLevelSelect(this, this::startEditor);
                currentScreen = editorLevelSelectScreen;
                break;
        }
        if (changeTo) {
            this.setScreen(currentScreen);
            Gdx.input.setInputProcessor(currentScreen.getStage());
        }
    }

    public void startEditor(M_Map map) {
        editor = new C_Editor(this, map);
        setupScreen(ScreenId.EDITOR);
        editor.setView(editorScreen);
        editorScreen.setController(editor);
        editorScreen.loadUI();
        navigateTo(ScreenId.EDITOR);
    }

    public void startGame(M_Map map) {
        //Loads 3d Models -> May be put in loading screen later.
        Model3DFactory.setup();

        game = new C_Game(map);
        setupScreen(ScreenId.GAME);
        game.setView(gameScreen);
        gameScreen.setController(game);
        game.start();
        navigateTo(ScreenId.GAME);
        Gdx.input.setInputProcessor(gameScreen.new GameInputProcessor());
    }

}
