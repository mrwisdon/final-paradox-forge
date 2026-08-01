package io.github.ragecraft4reforged.runeforge;

public enum RuneCurrency {
    NONE(""),
    LAPIS("lapis"),
    AMETHYST("amethyst");

    private final String serializedName;

    RuneCurrency(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getTranslationKey() {
        return "tooltip.ragecraft4reforged.rune.currency." + serializedName;
    }
}
