package io.github.ragecraft4reforged.runeforge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class PrefixSigilItem extends Item {
    private final PrefixTool tool;

    public PrefixSigilItem(PrefixTool tool) {
        super(new Item.Properties().stacksTo(1));
        this.tool = tool;
    }

    public PrefixTool tool() {
        return tool;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(style -> style.withColor(tool.color()).withBold(true).withItalic(false));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ragecraft4reforged.prefix_sigil.use")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.ragecraft4reforged.prefix_sigil.reusable")
                .withStyle(ChatFormatting.GRAY));
    }
}
