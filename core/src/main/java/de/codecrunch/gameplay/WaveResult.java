package de.codecrunch.gameplay;

public enum WaveResult {
    IN_PROGRESS("Wave: active"),
    VICTORY("Victory!"),
    DEFEAT("Defeat");

    private final String displayText;

    WaveResult(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
