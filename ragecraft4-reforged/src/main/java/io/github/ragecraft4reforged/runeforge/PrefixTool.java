package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;

public enum PrefixTool {
    SWORD("sword", 0xE36A62),
    AXE("axe", 0x67BBD1),
    BOW("bow", 0xD6A65A),
    PICKAXE("pickaxe", 0x9A78D1),
    ARMOR("armor", 0xD4D7E1);

    private final String id;
    private final int color;
    private final TagKey<Item> targetTag;

    PrefixTool(String id, int color) {
        this.id = id;
        this.color = color;
        this.targetTag = TagKey.create(Registries.ITEM,
                new ResourceLocation(Ragecraft4Reforged.MOD_ID, "prefix_targets/" + id));
    }

    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    public boolean matches(ItemStack stack) {
        return stack.is(targetTag) || switch (this) {
            case SWORD -> stack.getItem() instanceof SwordItem;
            case AXE -> stack.getItem() instanceof AxeItem;
            case BOW -> stack.getItem() instanceof BowItem;
            case PICKAXE -> stack.getItem() instanceof PickaxeItem;
            case ARMOR -> stack.getItem() instanceof ArmorItem;
        };
    }
}
