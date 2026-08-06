package de.codecrunch.controller;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import de.codecrunch.persistence.SettingsStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class C_SettingsTest {
    private InMemorySettingsStore store;
    private C_Settings settings;

    @Before
    public void setUp() {
        store = new InMemorySettingsStore();
        settings = new C_Settings(store);
    }

    @Test
    public void usesGameDefaultsWhenNothingIsStored() {
        assertEquals(0.5f, settings.getMusicVolume(), 0f);
        assertEquals(0.5f, settings.getSoundVolume(), 0f);
        assertTrue(settings.isPushEnabled());
    }

    @Test
    public void writesGameSettingsThroughTheStore() {
        settings.setMusicVolume(0.7f);
        settings.setSoundVolume(0.2f);
        settings.setPushEnabled(false);

        assertEquals(0.7f, settings.getMusicVolume(), 0f);
        assertEquals(0.2f, settings.getSoundVolume(), 0f);
        assertFalse(settings.isPushEnabled());
        assertTrue(store.floatValues.containsKey("volume"));
        assertTrue(store.floatValues.containsKey("sound"));
        assertTrue(store.booleanValues.containsKey("push.enabled"));
    }

    private static class InMemorySettingsStore implements SettingsStore {
        private final Map<String, Boolean> booleanValues = new HashMap<>();
        private final Map<String, Float> floatValues = new HashMap<>();

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return booleanValues.containsKey(key) ? booleanValues.get(key) : defaultValue;
        }

        @Override
        public float getFloat(String key, float defaultValue) {
            return floatValues.containsKey(key) ? floatValues.get(key) : defaultValue;
        }

        @Override
        public void putBoolean(String key, boolean value) {
            booleanValues.put(key, value);
        }

        @Override
        public void putFloat(String key, float value) {
            floatValues.put(key, value);
        }
    }
}
