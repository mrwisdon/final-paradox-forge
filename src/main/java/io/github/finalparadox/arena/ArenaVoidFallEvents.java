package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Applies void-specific safety only inside the new active arena dimension. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class ArenaVoidFallEvents {
    static final double FALL_THRESHOLD = 32.0D;

    private ArenaVoidFallEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || !ModDimensions.isActiveArena(player.serverLevel().dimension())
                || player.getY() >= FALL_THRESHOLD
                || player.isSpectator()) {
            return;
        }

        ServerLevel arena = player.serverLevel();
        boolean participant = ArenaFightParticipants.isInAnyFight(arena, player.getUUID());
        ArenaSlots.ArenaSlot slot = validCurrentSlot(player, arena);
        switch (ArenaVoidFallPolicy.decide(participant, slot != null)) {
            case NORMAL_DEATH -> {
                if (ArenaPlayerState.beginVoidGuard(player, 20)) {
                    player.hurt(player.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                }
            }
            case RESCUE_TO_ENTRY -> {
                if (!ArenaPlayerState.beginVoidGuard(player, 40)) return;
                BlockPos entry = slot.entryPosition();
                arena.getChunkAt(entry);
                player.teleportTo(arena, entry.getX() + 0.5D, entry.getY(), entry.getZ() + 0.5D,
                        slot.entryYaw(), player.getXRot());
                player.setDeltaMovement(0.0D, 0.0D, 0.0D);
                player.fallDistance = 0.0F;
                player.displayClientMessage(Component.translatable(
                        "message.finalparadox.arena_void.rescued"), true);
            }
            case RETURN_TO_OVERWORLD -> {
                if (ArenaPlayerState.beginVoidGuard(player, 40)) {
                    ArenaReturnService.returnToOverworld(
                            player, "message.finalparadox.arena_void.returned");
                }
            }
        }
    }

    private static ArenaSlots.ArenaSlot validCurrentSlot(ServerPlayer player, ServerLevel arena) {
        ArenaSlots.ArenaSlot slot = ArenaPlayerState.current(player).orElse(null);
        if (slot == null) return null;
        ArenaDeploymentData data = ArenaDeploymentData.get(arena, slot.definition());
        ArenaSandboxTravelDecision.Decision decision = ArenaSandboxTravelDecision.decide(
                data.state(), data.arenaId(), data.floorAnchor().orElse(null), slot);
        return decision == ArenaSandboxTravelDecision.Decision.TELEPORT ? slot : null;
    }
}
