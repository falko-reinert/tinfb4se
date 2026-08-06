package de.codecrunch.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import java.util.LinkedList;
import java.util.List;

public class RenderBatch extends ModelBatch {
    private final List<ModelInstance> instances = new LinkedList<>();

    public void renderAll(Environment environment) {
        for (ModelInstance instance : instances) render(instance, environment);
    }

    public void addElement(ModelInstance instance) {
        instances.add(instance);
    }
}
