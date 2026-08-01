package io.github.finalparadox.item;

import io.github.finalparadox.ability.GlaivorusAbilityState;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class KoyomiOmegaTridentItem extends TridentItem {
    public static final String CHARGES = "finalparadox.omega_charges";
    public static final String CHARGE_TIME = "finalparadox.omega_charge_time";
    public static final String LAST_CHARGE_TICK = "finalparadox.omega_last_charge_tick";

    private final boolean reforged;

    public KoyomiOmegaTridentItem() {
        this(false);
    }

    protected KoyomiOmegaTridentItem(boolean reforged) {
        super(new Properties().stacksTo(1).durability(250).fireResistant());
        this.reforged = reforged;
    }

    public boolean isReforged() {
        return reforged;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepare(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        prepare(stack);
        if (entity instanceof ServerPlayer player && !level.isClientSide) {
            tickCharge(player);
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    private void prepare(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", 4);
        int sharpness = reforged ? 8 : 4;
        int impaling = reforged ? 8 : 3;
        int loyalty = reforged ? 4 : 3;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        enchantments.put(Enchantments.SHARPNESS,
                Math.max(sharpness, enchantments.getOrDefault(Enchantments.SHARPNESS, 0)));
        enchantments.put(Enchantments.IMPALING,
                Math.max(impaling, enchantments.getOrDefault(Enchantments.IMPALING, 0)));
        enchantments.put(Enchantments.LOYALTY,
                Math.max(loyalty, enchantments.getOrDefault(Enchantments.LOYALTY, 0)));
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    private static void tickCharge(ServerPlayer player) {
        if (player.getPersistentData().getInt(LAST_CHARGE_TICK) == player.tickCount) {
            return;
        }
        player.getPersistentData().putInt(LAST_CHARGE_TICK, player.tickCount);
        int charges = player.getPersistentData().getInt(CHARGES);
        if (charges >= 3) {
            showCharges(player, charges);
            return;
        }
        boolean enemyNear = !player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(14), entity -> GlaivorusAbilityState.isHostileTarget(player, entity)).isEmpty();
        int time = player.getPersistentData().getInt(CHARGE_TIME) + (enemyNear ? 1 : 3);
        int threshold = hasReforgedTrident(player) ? 200 : 240;
        if (time >= threshold) {
            charges++;
            time = 0;
            player.getPersistentData().putInt(CHARGES, charges);
            player.level().playSound(null, player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ENDER_EYE_DEATH,
                    net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.0D, player.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.0D);
        }
        player.getPersistentData().putInt(CHARGE_TIME, time);
        showCharges(player, charges);
    }

    private static boolean hasReforgedTrident(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(ModItems.REFORGED_OMEGA_TRIDENT.get())) {
                return true;
            }
        }
        return false;
    }

    public static void showCharges(ServerPlayer player, int charges) {
        player.displayClientMessage(Component.translatable("message.finalparadox.omega.charges", bar(charges)), true);
    }

    private static String bar(int charges) {
        return (charges > 0 ? "§3🔱" : "§8🔱") + " §7- "
                + (charges > 1 ? "§3🔱" : "§8🔱") + " §7- "
                + (charges > 2 ? "§b🔱" : "§8🔱");
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        String prefix = reforged
                ? "item.finalparadox.reforged_omega_trident.lore."
                : "item.finalparadox.koyomi_omega_trident.lore.";
        for (int line = 1; line <= 14; line++) {
            tooltip.add(Component.translatable(prefix + line));
            if (line == 4 || line == 7 || line == 12) {
                tooltip.add(Component.empty());
            }
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.legendary"));
    }
}
