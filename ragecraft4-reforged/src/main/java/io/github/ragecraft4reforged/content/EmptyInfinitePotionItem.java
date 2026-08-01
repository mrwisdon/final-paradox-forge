package io.github.ragecraft4reforged.content;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** The original-map empty bottle placeholder held while the shared potion cooldown runs. */
public final class EmptyInfinitePotionItem extends Item {
    public EmptyInfinitePotionItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public ItemStack forType(int potionType) {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateTag().putBoolean(InfinitePotionItem.INFINITE_POTION, true);
        stack.getOrCreateTag().putInt(InfinitePotionItem.POTION_TYPE, potionType);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        InfinitePotionDefinition definition = definition(stack);
        return definition == null ? super.getName(stack) : InfinitePotionText.name(definition);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        InfinitePotionDefinition definition = definition(stack);
        if (definition != null) {
            InfinitePotionText.appendLore(definition, tooltip);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    public static InfinitePotionDefinition definition(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        return InfinitePotionCatalog.BY_TYPE.get(stack.getTag().getInt(InfinitePotionItem.POTION_TYPE));
    }
}
