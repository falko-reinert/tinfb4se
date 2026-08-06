package de.codecrunch.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.Map;

import de.codecrunch.Model3DFactory;
import de.codecrunch.model.ME_TileState;
import de.codecrunch.model.M_Tile;

public class TileVisualFactory implements Disposable {
    private static final float TILE_DISTANCE = 10f;

    private final Map<ME_TileState, Texture> editorTextures = new EnumMap<>(ME_TileState.class);

    public Image createEditorImage(M_Tile tile) {
        Image image = new Image();
        updateEditorImage(image, tile);
        image.setSize(image.getPrefWidth(), image.getPrefHeight());
        image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
        return image;
    }

    public void updateEditorImage(Image image, M_Tile tile) {
        Texture texture = editorTextures.get(tile.getTileState());
        if (texture == null) {
            texture = new Texture(editorTexturePath(tile.getTileState()));
            editorTextures.put(tile.getTileState(), texture);
        }
        image.setDrawable(new TextureRegionDrawable(new TextureRegion(texture)));
        image.setRotation(tile.getTileRotation());
    }

    public ModelInstance createGameModel(M_Tile tile) {
        ModelInstance instance = Model3DFactory.instantiate(gameModel(tile.getTileState()));
        if (tile.getTileState() == ME_TileState.PATH_LEFT) {
            instance.transform.rotate(Vector3.Y, -90);
        }
        instance.transform.rotate(Vector3.Y, tile.getTileRotation());
        instance.transform.setTranslation(tile.x_pos * TILE_DISTANCE, 0, tile.y_pos * TILE_DISTANCE);
        return instance;
    }

    private String editorTexturePath(ME_TileState state) {
        switch (state) {
            case START:
                return "tiles/textures/start.png";
            case END:
                return "tiles/textures/end.png";
            case PATH_STRAIGHT:
                return "tiles/textures/path_straight.png";
            case PATH_LEFT:
                return "tiles/textures/path_left.png";
            case PATH_RIGHT:
                return "tiles/textures/path_right.png";
            case PATH_CROSS:
                return "tiles/textures/path_cross.png";
            case PATH_SPLIT_RIGHT:
                return "tiles/textures/path_split_right.png";
            case PATH_SPLIT_LEFT:
                return "tiles/textures/path_split_left.png";
            case EMPTY:
            case OCCUPIED:
            default:
                return "tiles/textures/empty.png";
        }
    }

    private Model gameModel(ME_TileState state) {
        switch (state) {
            case START:
                return Model3DFactory.start;
            case END:
                return Model3DFactory.end;
            case PATH_STRAIGHT:
                return Model3DFactory.path_straight;
            case PATH_LEFT:
            case PATH_RIGHT:
                return Model3DFactory.path_corner;
            case PATH_CROSS:
                return Model3DFactory.path_cross;
            case PATH_SPLIT_RIGHT:
                return Model3DFactory.path_split_right;
            case PATH_SPLIT_LEFT:
                return Model3DFactory.path_split_left;
            case EMPTY:
            case OCCUPIED:
            default:
                return Model3DFactory.empty;
        }
    }

    @Override
    public void dispose() {
        for (Texture texture : editorTextures.values()) texture.dispose();
        editorTextures.clear();
    }
}
