package de.codecrunch;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.utils.UBJsonReader;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class Model3DFactory {
    public static Model empty,end,start,path_straight,path_corner,path_cross,path_split_right,path_split_left;
    private static G3dModelLoader modelLoader = new G3dModelLoader(new UBJsonReader());
    private static final Set<Model> ownedModels = Collections.newSetFromMap(new IdentityHashMap<Model, Boolean>());

    private Model3DFactory(){}

    public static void setup(){
        if (empty != null) return;

        empty = loadModel("tiles/models/empty.g3db");
        end = loadModel("tiles/models/end.g3db");
        start = loadModel("tiles/models/start.g3db");
        path_straight = loadModel("tiles/models/path_straight.g3db");
        path_corner = loadModel("tiles/models/path_corner.g3db");
        //path_cross = modelLoader.loadModel(Gdx.files.getFileHandle("tiles/models/path_cross.g3db", Files.FileType.Internal));
        //path_split_right = modelLoader.loadModel(Gdx.files.getFileHandle("tiles/models/path_split_right.g3db", Files.FileType.Internal));
        //path_split_left = modelLoader.loadModel(Gdx.files.getFileHandle("tiles/models/path_split_left.g3db", Files.FileType.Internal));
    }

    public static ModelInstance instantiate(Model model) {
        return new ModelInstance(model);
    }
    
    public static Model loadModel(String modelLoc){
		Model model = modelLoader.loadModel(Gdx.files.getFileHandle(modelLoc, Files.FileType.Internal));
        ownedModels.add(model);
        return model;
    }

    public static void dispose() {
        for (Model model : ownedModels) model.dispose();
        ownedModels.clear();
        empty = null;
        end = null;
        start = null;
        path_straight = null;
        path_corner = null;
        path_cross = null;
        path_split_right = null;
        path_split_left = null;
    }
}
