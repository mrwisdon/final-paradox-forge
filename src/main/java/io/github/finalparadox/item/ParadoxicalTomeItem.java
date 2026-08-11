package io.github.finalparadox.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class ParadoxicalTomeItem extends Item {
    public ParadoxicalTomeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack))
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC, ChatFormatting.BOLD);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 6; line++) {
            tooltip.add(Component.translatable("item.finalparadox.paradoxical_tome.lore." + line)
                    .withStyle(ChatFormatting.BLUE));
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.paradoxical_tome.cooldown")
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.paradoxical_tome.category")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
