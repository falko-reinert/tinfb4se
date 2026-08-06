package de.codecrunch.persistence;

import com.badlogic.gdx.Gdx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.codecrunch.model.M_Map;

public class LocalMapRepository implements MapRepository {
    private static final String MAP_DIRECTORY = "maps";
    private static final String MAP_EXTENSION = ".map";

    private final File directory;

    public LocalMapRepository() {
        this(Gdx.files.local(MAP_DIRECTORY).file());
    }

    public LocalMapRepository(File directory) {
        this.directory = directory;
    }

    @Override
    public List<String> getMapNames() {
        File[] files = directory.listFiles();
        if (files == null) return Collections.emptyList();

        List<String> names = new ArrayList<>();
        for (File file : files) {
            String fileName = file.getName();
            if (file.isFile() && fileName.endsWith(MAP_EXTENSION)) {
                names.add(fileName.substring(0, fileName.length() - MAP_EXTENSION.length()));
            }
        }
        return names;
    }

    @Override
    public M_Map load(String name) {
        File file = new File(directory, name + MAP_EXTENSION);
        if (!file.isFile()) return null;

        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))) {
            return (M_Map) stream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean save(M_Map map) {
        if (!directory.exists() && !directory.mkdirs()) return false;

        File file = new File(directory, map.getMapName() + MAP_EXTENSION);
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
            stream.writeObject(map);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
