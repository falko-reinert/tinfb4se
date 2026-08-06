package de.codecrunch;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import de.codecrunch.controller.C_Editor;
import de.codecrunch.controller.C_Game;
import de.codecrunch.controller.C_LevelSelect;
import de.codecrunch.controller.C_Settings;
import de.codecrunch.model.M_Map;
import de.codecrunch.persistence.GdxSettingsStore;
import de.codecrunch.persistence.LocalMapRepository;
import de.codecrunch.persistence.MapRepository;
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
    private C_Settings settings;
    private MapRepository mapRepository;

    private V_Game gameScreen;
    private V_GameLevelSelect levelSelectScreen;
    private V_Menu menuScreen;
    private V_Settings settingsScreen;
    private V_Editor editorScreen;
    private V_EditorLevelSelect editorLevelSelectScreen;


    @Override
    public void create() {
        settings = new C_Settings(new GdxSettingsStore());
        mapRepository = new LocalMapRepository();
        navigateTo(ScreenId.MENU);
    }

    @Override
    public void navigateTo(ScreenId screenId) {
        activateScreen(getOrCreateScreen(screenId));
    }

    private VA_Screen getOrCreateScreen(ScreenId screenId) {
        switch (screenId) {
            case GAME:
                if (gameScreen == null) gameScreen = new V_Game();
                return gameScreen;
            case GAME_LEVEL_SELECT:
                if (levelSelectScreen == null)
                    levelSelectScreen = new V_GameLevelSelect(this, this::startGame,
                            new C_LevelSelect(mapRepository));
                return levelSelectScreen;
            case MENU:
                if (menuScreen == null) menuScreen = new V_Menu(this);
                return menuScreen;
            case SETTINGS:
                if (settingsScreen == null) settingsScreen = new V_Settings(this, settings);
                return settingsScreen;
            case EDITOR:
                if (editorScreen == null) editorScreen = new V_Editor(this);
                return editorScreen;
            case EDITOR_LEVEL_SELECT:
                if (editorLevelSelectScreen == null)
                    editorLevelSelectScreen = new V_EditorLevelSelect(this, this::startEditor,
                            new C_LevelSelect(mapRepository));
                return editorLevelSelectScreen;
            default:
                throw new IllegalArgumentException("Unsupported screen: " + screenId);
        }
    }

    private void activateScreen(VA_Screen nextScreen) {
        setScreen(nextScreen);
        Gdx.input.setInputProcessor(nextScreen.getInputProcessor());
    }

    public void startEditor(M_Map map) {
        V_Editor nextScreen = new V_Editor(this);
        C_Editor nextEditor = new C_Editor(this, map, mapRepository);
        nextEditor.setView(nextScreen);
        nextScreen.setController(nextEditor);
        nextScreen.loadUI();

        V_Editor previousScreen = editorScreen;
        editor = nextEditor;
        editorScreen = nextScreen;
        activateScreen(nextScreen);
        disposeScreen(previousScreen);
    }

    public void startGame(M_Map map) {
        //Loads 3d Models -> May be put in loading screen later.
        Model3DFactory.setup();

        V_Game nextScreen = new V_Game();
        C_Game nextGame = new C_Game(map);
        nextGame.setView(nextScreen);
        nextScreen.setController(nextGame);
        nextGame.start();

        C_Game previousGame = game;
        V_Game previousScreen = gameScreen;
        game = nextGame;
        gameScreen = nextScreen;
        activateScreen(nextScreen);
        disposeGameSession(previousGame, previousScreen);
    }

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        disposeGameSession(game, gameScreen);
        disposeScreen(editorScreen);
        disposeScreen(levelSelectScreen);
        disposeScreen(menuScreen);
        disposeScreen(settingsScreen);
        disposeScreen(editorLevelSelectScreen);
        Model3DFactory.dispose();
        VA_Screen.disposeSharedResources();
    }

    private void disposeGameSession(C_Game gameController, V_Game screen) {
        if (gameController != null) gameController.dispose();
        disposeScreen(screen);
    }

    private void disposeScreen(VA_Screen screen) {
        if (screen != null) screen.dispose();
    }

}
