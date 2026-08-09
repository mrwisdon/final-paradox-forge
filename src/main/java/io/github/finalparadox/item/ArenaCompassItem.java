package io.github.finalparadox.item;

import io.github.finalparadox.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Reusable diamond compass that travels between the overworld and the arena
 * dimension. Right-clicking enters the arena; using it again returns home.
 */
public final class ArenaCompassItem extends Item {
    public ArenaCompassItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.pass(stack);

        ServerLevel current = server.serverLevel();
        boolean returning = ModDimensions.ARENA_DIMENSION.equals(current.dimension());
        ServerLevel target = returning
                ? server.getServer().overworld()
                : server.getServer().getLevel(ModDimensions.ARENA_DIMENSION);
        if (target == null) {
            server.displayClientMessage(
                    Component.translatable("message.finalparadox.arena_compass.missing"), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!returning && stack.getTag() != null
                && stack.getTag().contains(ArenaCompassDestination.RESPAWN_TAG)) {
            int[] destination = stack.getTag().getIntArray(ArenaCompassDestination.RESPAWN_TAG);
            if (destination.length == 3) {
                float yaw = stack.getTag().getFloat(ArenaCompassDestination.RESPAWN_YAW_TAG);
                server.teleportTo(target,
                        destination[0] + 0.5D, destination[1], destination[2] + 0.5D,
                        yaw, server.getXRot());
                target.playSound(null, server.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                server.displayClientMessage(Component.translatable(
                        "message.finalparadox.arena_compass.respawn"), true);
                server.getCooldowns().addCooldown(this, 40);
                return InteractionResultHolder.consume(stack);
            }
        }

        BlockPos spawn = target.getSharedSpawnPos();
        float yaw = server.getYRot();
        if (returning && server.getRespawnPosition() != null
                && Level.OVERWORLD.equals(server.getRespawnDimension())) {
            spawn = server.getRespawnPosition();
            yaw = server.getRespawnAngle();
        }
        int ground = target.getHeight(
                Heightmap.Types.MOTION_BLOCKING, spawn.getX(), spawn.getZ());
        double x = spawn.getX() + 0.5D;
        double y = Math.max(ground + 1, spawn.getY() + 1);
        double z = spawn.getZ() + 0.5D;
        server.teleportTo(target, x, y, z, yaw, server.getXRot());
        target.playSound(null, server.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        server.displayClientMessage(Component.translatable(
                returning ? "message.finalparadox.arena_compass.return"
                        : "message.finalparadox.arena_compass.enter"), true);
        server.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("item.finalparadox.arena_compass.lore"));
    }
}
