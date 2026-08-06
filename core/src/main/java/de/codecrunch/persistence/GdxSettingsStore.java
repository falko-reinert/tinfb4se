package de.codecrunch.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GdxSettingsStore implements SettingsStore {
    private static final String PREFERENCES_NAME = "code_crunch.tower_attack";

    private final Preferences preferences;

    public GdxSettingsStore() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        preferences.putBoolean(key, value);
        preferences.flush();
    }

    @Override
    public void putFloat(String key, float value) {
        preferences.putFloat(key, value);
        preferences.flush();
    }
}
