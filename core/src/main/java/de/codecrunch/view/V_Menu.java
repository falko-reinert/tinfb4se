package de.codecrunch.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import de.codecrunch.ScreenId;
import de.codecrunch.ScreenNavigator;

public class V_Menu extends VA_Screen {
	private final ScreenNavigator screenNavigator;
	private final Runnable combatDemoStarter;
    public TextButton settings;

	public V_Menu(ScreenNavigator screenNavigator) {
		this(screenNavigator, () -> { });
	}

	public V_Menu(ScreenNavigator screenNavigator, Runnable combatDemoStarter) {
		super();
		this.screenNavigator = screenNavigator;
		this.combatDemoStarter = combatDemoStarter;
        Table table = new Table();
        stage.addActor(table);
        table.setFillParent(true);

		TextButton newGame = new TextButton("New Game", uiSkin);
		TextButton combatDemo = new TextButton("Combat Demo", uiSkin);
        TextButton editor = new TextButton("Editor", uiSkin);
        settings = new TextButton("Settings", uiSkin);
        TextButton exit = new TextButton("Exit", uiSkin);

        table.defaults().pad(10, 20, 10, 20).expand().fill();

		table.add(newGame).row();
		table.add(combatDemo).row();
        table.add(editor).row();
        table.add(settings).row();
        table.add(exit).row();

        editor.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screenNavigator.navigateTo(ScreenId.EDITOR_LEVEL_SELECT);
            }
        });

        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        settings.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screenNavigator.navigateTo(ScreenId.SETTINGS);
            }
        });

		newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screenNavigator.navigateTo(ScreenId.GAME_LEVEL_SELECT);
            }
		});

		combatDemo.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				V_Menu.this.combatDemoStarter.run();
			}
		});
    }

    @Override
    public void show() {

    }
}
