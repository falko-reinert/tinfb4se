package de.codecrunch.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import java.util.function.Consumer;

import de.codecrunch.ScreenId;
import de.codecrunch.ScreenNavigator;
import de.codecrunch.controller.C_LevelSelect;
import de.codecrunch.model.M_Map;

public class V_EditorLevelSelect extends VA_LevelSelect {

    private final ScreenNavigator screenNavigator;
    private final Consumer<M_Map> editorStarter;

    public V_EditorLevelSelect(ScreenNavigator screenNavigator, Consumer<M_Map> editorStarter,
                               C_LevelSelect controller) {
        super(controller);
        this.screenNavigator = screenNavigator;
        this.editorStarter = editorStarter;
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton newLevel = new TextButton("New Level", uiSkin);
        TextButton back = new TextButton("Back", uiSkin);

        table.add(newLevel).colspan(2).row();
        addLevelButtons(table);
        table.add(back).colspan(2);

        newLevel.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                editorStarter.accept(null);
            }
        });

        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screenNavigator.navigateTo(ScreenId.MENU);
            }
        });
    }

    @Override
    public void startMap(M_Map map) {
        editorStarter.accept(map);
    }
}
