package io.github.finalparadox.item;

import io.github.finalparadox.arena.ArenaBossFightState;
import io.github.finalparadox.arena.ArenaReturnService;
import io.github.finalparadox.registry.ModDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Return compass for both the active and legacy arena dimensions. Initial
 * arena entry is intentionally owned only by the five boss sandboxes.
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
        ArenaCompassPolicy.Action action = ArenaCompassPolicy.decide(
                ModDimensions.isActiveArena(current.dimension()),
                ModDimensions.LEGACY_ARENA_DIMENSION.equals(current.dimension()),
                ModDimensions.isActiveArena(current.dimension()) && ArenaBossFightState.isAnyActive(current));
        if (action == ArenaCompassPolicy.Action.DIRECT_TO_SANDBOX) {
            server.displayClientMessage(Component.translatable(
                    "message.finalparadox.arena_compass.use_sandbox"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (action == ArenaCompassPolicy.Action.BLOCKED_BY_FIGHT) {
            server.displayClientMessage(
                    Component.translatable("message.finalparadox.arena_compass.bossfight_active"), true);
            return InteractionResultHolder.fail(stack);
        }
        ArenaReturnService.returnToOverworld(server, "message.finalparadox.arena_compass.return");
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
