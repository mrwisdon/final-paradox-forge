package io.github.finalparadox.item;

import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Recon drone controller. Right-clicking deploys a drone at the player's
 * current position, switches the camera to the drone, and keeps the player
 * body frozen on the ground until the drone is dismissed.
 */
public final class ReconDroneItem extends Item {
    public static final String COOLDOWN_KEY = "finalparadox.recon_drone_cooldown";

    public ReconDroneItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(player instanceof ServerPlayer server)) {
            return InteractionResultHolder.pass(stack);
        }
        if (server.getPersistentData().getInt(COOLDOWN_KEY) > 0) {
            server.displayClientMessage(
                    Component.translatable("message.finalparadox.recon_drone.cooldown"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (DroneEntity.isActiveFor(server)) {
            server.displayClientMessage(
                    Component.translatable("message.finalparadox.recon_drone.already_active"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!DroneEntity.deploy(server)) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int i = 1; i <= 4; i++) {
            tooltip.add(Component.translatable("item.finalparadox.recon_drone.lore." + i));
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.unique"));
    }
}
