package de.codecrunch.persistence;

public interface SettingsStore {
    boolean getBoolean(String key, boolean defaultValue);

    float getFloat(String key, float defaultValue);

    void putBoolean(String key, boolean value);

    void putFloat(String key, float value);
}
