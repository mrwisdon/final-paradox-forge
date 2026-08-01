package io.github.ragecraft4reforged.runeforge;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class SuffixSigilItem extends Item {
    private final SuffixSchool school;

    public SuffixSigilItem(SuffixSchool school) {
        super(new Item.Properties().stacksTo(1));
        this.school = school;
    }

    public SuffixSchool school() {
        return school;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(style -> style.withColor(school.color()).withBold(true).withItalic(false));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ragecraft4reforged.suffix_sigil.use")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.ragecraft4reforged.suffix_sigil.reusable")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
