package io.github.ragecraft4reforged.runeforge;

public enum RuneRarity {
    COMMON("common", 0xAAAAAA),
    UNCOMMON("uncommon", 0x269AFF),
    RARE("rare", 0x04BF36),
    EPIC("epic", 0xD19E13),
    LEGENDARY("legendary", 0x8633BD);

    private final String serializedName;
    private final int nameColor;

    RuneRarity(String serializedName, int nameColor) {
        this.serializedName = serializedName;
        this.nameColor = nameColor;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public int getNameColor() {
        return nameColor;
    }
}
