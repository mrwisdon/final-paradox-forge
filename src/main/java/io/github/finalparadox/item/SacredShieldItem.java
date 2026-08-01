package io.github.finalparadox.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;
import io.github.finalparadox.client.SacredShieldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class SacredShieldItem extends ShieldItem {
    public SacredShieldItem() { super(new Properties().stacksTo(1)); }

    @Override public ItemStack getDefaultInstance() { ItemStack stack = super.getDefaultInstance(); prepare(stack); return stack; }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) { prepare(stack); }
    private static void prepare(ItemStack stack) { stack.getOrCreateTag().putBoolean("Unbreakable", true); stack.getOrCreateTag().putInt("HideFlags", 4); }

    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SacredShieldRenderer renderer;
            @Override public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    renderer = new SacredShieldRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }
        });
    }

    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 4; line++) tooltip.add(Component.translatable("item.finalparadox.sacred_shield.lore." + line));
        tooltip.add(Component.empty()); tooltip.add(Component.translatable("item.finalparadox.unique"));
    }
}
