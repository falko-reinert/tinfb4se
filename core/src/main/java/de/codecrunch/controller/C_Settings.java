package de.codecrunch.controller;

import de.codecrunch.persistence.SettingsStore;

public class C_Settings {
    private static final String PREF_MUSIC_VOLUME = "volume";
    private static final String PREF_PUSH_ENABLED = "push.enabled";
    private static final String PREF_SOUND_VOL = "sound";

    private final SettingsStore store;

    public C_Settings(SettingsStore store) {
        this.store = store;
    }

    public boolean isPushEnabled() {
        return store.getBoolean(PREF_PUSH_ENABLED, true);
    }

    public void setPushEnabled(boolean pushEnabled) {
        store.putBoolean(PREF_PUSH_ENABLED, pushEnabled);
    }

    public float getMusicVolume() {
        return store.getFloat(PREF_MUSIC_VOLUME, 0.5f);
    }

    public void setMusicVolume(float volume) {
        store.putFloat(PREF_MUSIC_VOLUME, volume);
    }

    public float getSoundVolume() {
        return store.getFloat(PREF_SOUND_VOL, 0.5f);
    }

    public void setSoundVolume(float volume) {
        store.putFloat(PREF_SOUND_VOL, volume);
    }
}
