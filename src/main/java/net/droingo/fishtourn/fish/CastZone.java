package net.droingo.fishtourn.fish;

public enum CastZone {
    NONE("Open Water"),
    DEEP("Deep Zone");

    private final String displayName;

    CastZone(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}