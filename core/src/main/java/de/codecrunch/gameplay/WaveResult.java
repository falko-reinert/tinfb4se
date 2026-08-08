package de.codecrunch.gameplay;

public enum WaveResult {
    IN_PROGRESS("Wave: active"),
    COMPLETE("Wave complete!"),
    BASE_DESTROYED("Base destroyed");

    private final String displayText;

    WaveResult(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
