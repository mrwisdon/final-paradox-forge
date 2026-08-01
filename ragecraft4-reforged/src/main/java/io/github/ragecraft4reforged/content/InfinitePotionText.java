package io.github.ragecraft4reforged.content;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

final class InfinitePotionText {
    private static final String SPACE = "wtem.space";
    private static final String EXALTED_HEADING = "item.lingering_potion.1.lore.1.1";

    static Component name(InfinitePotionDefinition definition) {
        return Component.translatable(definition.nameKey()).withStyle(style -> style
                .withColor(definition.nameColor()).withBold(true).withItalic(false));
    }

    static void appendLore(InfinitePotionDefinition definition, List<Component> tooltip) {
        boolean flavor = false;
        for (int index = 0; index < definition.loreKeys().size(); index++) {
            String key = definition.loreKeys().get(index);
            if (SPACE.equals(key)) {
                tooltip.add(Component.translatable(key));
                flavor = true;
            } else if (index == 0) {
                tooltip.add(Component.translatable(key).withStyle(style -> style
                        .withColor(ChatFormatting.WHITE).withBold(true).withItalic(false)));
            } else if (EXALTED_HEADING.equals(key)) {
                tooltip.add(Component.translatable(key).withStyle(style -> style
                        .withColor(0xB973FF).withBold(true).withItalic(false)));
            } else if (flavor) {
                tooltip.add(Component.translatable(key).withStyle(style -> style
                        .withColor(ChatFormatting.DARK_GRAY).withBold(false).withItalic(true)));
            } else {
                tooltip.add(Component.translatable(key).withStyle(style -> style
                        .withColor(ChatFormatting.GRAY).withItalic(false)));
            }
        }
    }

    private InfinitePotionText() {
    }
}
