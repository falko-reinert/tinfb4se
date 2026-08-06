package de.codecrunch.controller;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import de.codecrunch.ScreenId;
import de.codecrunch.ScreenNavigator;
import de.codecrunch.editor.EditorRules;
import de.codecrunch.model.M_Map;
import de.codecrunch.model.M_Tile;
import de.codecrunch.persistence.MapRepository;
import de.codecrunch.view.V_Editor;

public class C_Editor {

	private V_Editor view;
	private boolean additive = true;
	private ScreenNavigator screenNavigator;
	private final MapRepository mapRepository;
	private final EditorRules rules;

	public C_Editor(ScreenNavigator screenNavigator, M_Map m, MapRepository mapRepository) {
		this.screenNavigator = screenNavigator;
		this.mapRepository = mapRepository;
		rules = new EditorRules(m);
	}

	public void setView(V_Editor view) {
		this.view = view;
		rules.getMap().foreachTile(view::updateTile);
	}

	public M_Map getMap() {
		return rules.getMap();
	}

	public void setAdditive(boolean add) {
		additive = add;
	}

	public void clickedOnTile(M_Tile tile) {
		if (additive)
			addToPath(tile);
		else
			removeFromPath(tile);
	}

	public void addToPath(M_Tile tile) {
		updateView(rules.addToPath(tile));
	}

	public void removeFromPath(M_Tile tile) {
		updateView(rules.removeFromPath(tile));
	}

	private void updateView(Iterable<M_Tile> tiles) {
		if (view != null) view.updateTiles(tiles);
	}

	public void save() {
		TextField input = new TextField(rules.getMap().getMapName(), view.uiSkin);
		input.setMaxLength(9);
		Dialog dialog = new Dialog("Save Level", view.uiSkin) {
			@Override
			public void result(Object obj) {
				if ((boolean) obj) {
					saveLevel(input.getText());
				}
			}
		};
		dialog.text("Enter a level name:");
		dialog.getContentTable().row();
		dialog.getContentTable().add(input);
		dialog.button("Done", true);
		dialog.button("Back", false);
		view.showDialog(dialog);
	}

	public void saveLevel(String levelName) {
		if (!rules.prepareMapForSave(levelName)) return;
		mapRepository.save(rules.getMap());
		screenNavigator.navigateTo(ScreenId.EDITOR_LEVEL_SELECT);
	}
}
