package de.codecrunch.view;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.Vector3;

import de.codecrunch.gameplay.PathUnit;
import de.codecrunch.gameplay.UnitOutcome;
import de.codecrunch.gameplay.WaveResult;

public class V_HUD implements Disposable {
    public Stage stage;
    private Viewport viewport;
    private final BitmapFont font;
    private final Label baseIntegrityLabel;
    private final Label unitOutcomeLabel;
    private final Label waveResultLabel;
    private final Vector3 projectedUnitPosition = new Vector3();

    public V_HUD(SpriteBatch sb, final Runnable exitGameAction, final Runnable replayAction){
        viewport = new FitViewport(1024,576, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        font = new BitmapFont();
        baseIntegrityLabel = new Label("Base integrity: 3", new Label.LabelStyle(font, Color.WHITE));
        baseIntegrityLabel.setPosition(20f, viewport.getWorldHeight() - 35f);
        stage.addActor(baseIntegrityLabel);
        unitOutcomeLabel = new Label("Last unit: -", new Label.LabelStyle(font, Color.WHITE));
        unitOutcomeLabel.setPosition(20f, viewport.getWorldHeight() - 65f);
        stage.addActor(unitOutcomeLabel);
        waveResultLabel = new Label(WaveResult.IN_PROGRESS.getDisplayText(),
                new Label.LabelStyle(font, Color.WHITE));
        waveResultLabel.setPosition(20f, viewport.getWorldHeight() - 95f);
        stage.addActor(waveResultLabel);
        TextButton exitGame = new TextButton("Exit game", VA_Screen.uiSkin);
        exitGame.setPosition(20f, 20f);
        exitGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                exitGameAction.run();
            }
        });
        stage.addActor(exitGame);
        TextButton replay = new TextButton("Replay", VA_Screen.uiSkin);
        replay.setPosition(160f, 20f);
        replay.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                replayAction.run();
            }
        });
        stage.addActor(replay);
    }

    public void setBaseIntegrity(int baseIntegrity) {
        baseIntegrityLabel.setText("Base integrity: " + baseIntegrity);
    }

    public void setUnitOutcome(UnitOutcome outcome) {
        unitOutcomeLabel.setText("Last unit: " + outcome.getDisplayText());
    }

    public void setWaveResult(WaveResult result) {
        waveResultLabel.setText(result.getDisplayText());
        waveResultLabel.setColor(result == WaveResult.BASE_DESTROYED ? Color.RED : Color.WHITE);
    }

    public void drawUnitHealth(PerspectiveCamera camera, Iterable<PathUnit> units) {
        SpriteBatch batch = (SpriteBatch) stage.getBatch();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        for (PathUnit unit : units) {
            projectedUnitPosition.set(unit.getX() * 10f, 2f, unit.getY() * 10f);
            camera.project(projectedUnitPosition);
            viewport.unproject(projectedUnitPosition);
            font.draw(batch, "HP: " + unit.getUnit().getCurrentLife(),
                    projectedUnitPosition.x - 20f, projectedUnitPosition.y + 15f);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
    }
}
