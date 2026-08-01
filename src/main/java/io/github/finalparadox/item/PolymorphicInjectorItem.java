package io.github.finalparadox.item;

import io.github.finalparadox.event.GameplayEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import java.util.List;

public final class PolymorphicInjectorItem extends CrossbowItem {
    public static final String COOLDOWN_KEY = "finalparadox.polymorphic_injector_cooldown";
    public static final String ARROW_KEY = "finalparadox.polymorphic_arrow";
    public PolymorphicInjectorItem() { super(new Properties().stacksTo(1)); }

    public static void tickCooldown(ServerPlayer player) {
        int ticks = player.getPersistentData().getInt(COOLDOWN_KEY);
        if (ticks <= 0) return;
        if (--ticks > 0) { player.getPersistentData().putInt(COOLDOWN_KEY, ticks); return; }
        player.getPersistentData().remove(COOLDOWN_KEY);
        player.level().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 1.0F, 1.5F);
        player.displayClientMessage(Component.translatable("message.finalparadox.polymorphic_injector.ready"), true);
    }

    @Override public ItemStack getDefaultInstance() { ItemStack stack = super.getDefaultInstance(); prepare(stack); return stack; }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) { prepare(stack); }
    private static void prepare(ItemStack stack) { stack.getOrCreateTag().putBoolean("Unbreakable", true); stack.getOrCreateTag().putInt("HideFlags", 4); if (stack.getEnchantmentLevel(Enchantments.QUICK_CHARGE) < 2) stack.enchant(Enchantments.QUICK_CHARGE, 2); }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) { tooltip.add(Component.empty()); for (int i=1;i<=8;i++) tooltip.add(Component.translatable("item.finalparadox.polymorphic_injector.lore."+i)); tooltip.add(Component.empty()); tooltip.add(Component.translatable("item.finalparadox.unique")); }
}
