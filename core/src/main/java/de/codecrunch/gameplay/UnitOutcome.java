package de.codecrunch.gameplay;

public enum UnitOutcome {
    DEFEATED("Unit defeated"),
    ESCAPED("Unit escaped");

    private final String displayText;

    UnitOutcome(String displayText) {
        this.displayText = displayText;
    }

    public String getDisplayText() {
        return displayText;
    }
}
