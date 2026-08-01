package io.github.ragecraft4reforged.runeforge;

public enum RuneCategory {
    PREFIX("prefix"),
    UPGRADE("upgrade"),
    SUFFIX("suffix");

    private final String serializedName;

    RuneCategory(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

}
