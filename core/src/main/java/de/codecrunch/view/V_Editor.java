package de.codecrunch.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.IdentityHashMap;
import java.util.Map;

import de.codecrunch.ScreenId;
import de.codecrunch.ScreenNavigator;
import de.codecrunch.controller.C_Editor;
import de.codecrunch.model.M_Tile;
import de.codecrunch.render.TileVisualFactory;

public class V_Editor extends VA_Screen {
	private C_Editor controller;
	private final ScreenNavigator screenNavigator;
	private final TileVisualFactory tileVisualFactory = new TileVisualFactory();
	private final Map<M_Tile, Image> tileImages = new IdentityHashMap<>();

	public V_Editor(ScreenNavigator screenNavigator) {
		super();
		this.screenNavigator = screenNavigator;
	}

	public V_Editor loadUI() {
		Table uiTable = new Table();
		Table mapTable = new Table();

		stage.addActor(uiTable);
		uiTable.setFillParent(true);
		uiTable.add(mapTable).colspan(4);

		M_Tile[][] tiles = controller.getMap().getAllTiles();
		for (int y = 0; y < controller.getMap().y_count; y++) {
			for (int x = 0; x < controller.getMap().x_count; x++) {
				final M_Tile m_tile = tiles[x][y];
				Image tileImage = tileVisualFactory.createEditorImage(m_tile);
				tileImages.put(m_tile, tileImage);
				mapTable.add(tileImage).center();
				tileImage.addListener(new InputListener() {
					@Override
					public void enter(InputEvent event, float x, float y, int pointer,
							Actor fromActor) {
						if (pointer >= 0) controller.clickedOnTile(m_tile);
					}
				});
			}
			mapTable.row();
		}
		uiTable.row().padTop(20);

		TextButton add = new TextButton("ADD", uiSkin);
		TextButton remove = new TextButton("REMOVE", uiSkin);
		TextButton exit = new TextButton("Exit", uiSkin);
		TextButton save = new TextButton("Save", uiSkin);

		uiTable.add(add).right();
		uiTable.add(remove).left();
		uiTable.add(exit).right();
		uiTable.add(save).left();

		add.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				controller.setAdditive(true);
			}
		});
		remove.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				controller.setAdditive(false);
			}
		});
		exit.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				screenNavigator.navigateTo(ScreenId.EDITOR_LEVEL_SELECT);
			}
		});
		save.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				controller.save();
			}
		});
		return this;
	}

	public void setController(C_Editor controller) {
		this.controller = controller;
	}

	public void updateTile(M_Tile tile) {
		Image image = tileImages.get(tile);
		if (image != null) tileVisualFactory.updateEditorImage(image, tile);
	}

	public void updateTiles(Iterable<M_Tile> tiles) {
		for (M_Tile tile : tiles) updateTile(tile);
	}

	@Override
	public void dispose() {
		super.dispose();
		tileVisualFactory.dispose();
	}

	@Override
	public void show() {

	}

}
