package io.github.finalparadox.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.finalparadox.entity.StygianArrowEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public final class StygianPointItem extends BowItem {
    private static final String COOLDOWN_KEY = "finalparadox.stygian_point_cooldown";
    private static final int SPECIAL_COOLDOWN_TICKS = 171;
    private static final int HIDE_UNBREAKABLE = 4;
    private static final DustParticleOptions PURPLE_DUST = new DustParticleOptions(new Vector3f(0.663F, 0.416F, 0.686F), 2.0F);
    private static final DustParticleOptions DARK_DUST = new DustParticleOptions(new Vector3f(0.192F, 0.169F, 0.192F), 2.0F);
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("b7457748-efa4-4232-be15-4b2d33533fe2");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("23d09d51-2934-47d1-b3f2-935c59578a45");
    private final Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public StygianPointItem() {
        super(new Properties().durability(384));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_UUID,
                "Stygian Point attack damage", 5.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_UUID,
                "Stygian Point attack speed", -2.0D, AttributeModifier.Operation.ADDITION));
        mainHandModifiers = builder.build();
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow original) {
        if (!(original.getOwner() instanceof ServerPlayer player) || player.onGround()
                || player.getPersistentData().getInt(COOLDOWN_KEY) > 0) {
            return original;
        }

        StygianArrowEntity arrow = new StygianArrowEntity(player.level(), player);
        arrow.setPos(original.getX(), original.getY(), original.getZ());
        arrow.setBaseDamage(original.getBaseDamage());
        arrow.setNoGravity(true);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        player.getPersistentData().putInt(COOLDOWN_KEY, SPECIAL_COOLDOWN_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SHOOT,
                SoundSource.PLAYERS, 0.4F, 1.4F);
        return arrow;
    }

    public static void tickCooldown(ServerPlayer player) {
        int remaining = player.getPersistentData().getInt(COOLDOWN_KEY);
        if (remaining <= 0) return;
        remaining--;
        if (remaining > 0) {
            player.getPersistentData().putInt(COOLDOWN_KEY, remaining);
            return;
        }

        player.getPersistentData().remove(COOLDOWN_KEY);
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.6F);
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.6F);
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(PURPLE_DUST, player.getX(), player.getY() + 1.2D, player.getZ(),
                10, 0.5D, 0.7D, 0.5D, 0.0D);
        level.sendParticles(DARK_DUST, player.getX(), player.getY() + 1.2D, player.getZ(),
                12, 0.5D, 0.7D, 0.5D, 0.0D);
        player.displayClientMessage(Component.translatable("message.finalparadox.stygian_point.ready")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepareStack(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        prepareStack(stack);
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    private static void prepareStack(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", HIDE_UNBREAKABLE);
        if (stack.getEnchantmentLevel(Enchantments.POWER_ARROWS) < 2) {
            stack.enchant(Enchantments.POWER_ARROWS, 2);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mainHandModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 8; line++) {
            tooltip.add(Component.translatable("item.finalparadox.stygian_point.lore." + line));
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.stygian_point.unique"));
    }
}
