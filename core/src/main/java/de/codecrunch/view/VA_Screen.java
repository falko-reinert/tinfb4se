package de.codecrunch.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class VA_Screen implements Screen {

    Stage stage;
    final public static Skin uiSkin = new Skin(Gdx.files.internal("skins/quantum-horizon/quantum-horizon-ui.json"));

    public VA_Screen() {
        stage = new Stage(new FitViewport(640, 360));
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public InputProcessor getInputProcessor() {
        return stage;
    }

    public static void disposeSharedResources() {
        uiSkin.dispose();
    }

    public void showDialog(Dialog dialog) {
        dialog.show(stage);
    }
}
