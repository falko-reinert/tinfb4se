package de.codecrunch.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;

public class V_HUD implements Disposable {
    public Stage stage;
    private Viewport viewport;

    public V_HUD(SpriteBatch sb){
        viewport = new FitViewport(1024,576, new OrthographicCamera());
        stage = new Stage(viewport, sb);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
