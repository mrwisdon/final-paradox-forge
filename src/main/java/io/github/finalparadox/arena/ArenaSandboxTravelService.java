package io.github.finalparadox.arena;

import io.github.finalparadox.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** Server-authoritative sandbox deployment queue and exact-entry teleporter. */
public final class ArenaSandboxTravelService {
    private ArenaSandboxTravelService() {
    }

    public static boolean request(ServerPlayer player, String arenaId) {
        ArenaSlots.ArenaSlot slot = ArenaSlots.forArenaId(arenaId).orElse(null);
        if (slot == null) {
            player.displayClientMessage(Component.translatable(
                    "message.finalparadox.arena_sandbox.unknown"), true);
            return false;
        }
        ServerLevel arena = player.getServer().getLevel(ModDimensions.ARENA_VOID);
        if (arena == null) {
            player.displayClientMessage(Component.translatable(
                    "message.finalparadox.arena_sandbox.dimension_missing"), true);
            return false;
        }
        if (!ArenaSandboxAccessPolicy.mayRequest(
                ModDimensions.isActiveArena(player.serverLevel().dimension()),
                ArenaBossFightState.isAnyActive(arena))) {
            player.displayClientMessage(Component.translatable(
                    "message.finalparadox.arena_compass.bossfight_active"), true);
            return false;
        }

        ArenaDeploymentData data = ArenaDeploymentData.get(arena, slot.definition());
        ArenaSandboxTravelDecision.Decision decision = ArenaSandboxTravelDecision.decide(
                data.state(), data.arenaId(), data.floorAnchor().orElse(null), slot);
        return switch (decision) {
            case TELEPORT -> {
                teleport(player, arena, slot);
                yield true;
            }
            case START_AND_WAIT -> beginAndWait(player, arena, slot, data);
            case WAIT -> {
                ArenaPlayerState.waitFor(player, slot);
                player.displayClientMessage(Component.translatable(
                        "message.finalparadox.arena_sandbox.loading", slot.definition().id().toUpperCase()), true);
                yield true;
            }
            case REJECT_ERROR -> {
                ArenaPlayerState.clearPending(player);
                player.displayClientMessage(Component.translatable(
                        "message.finalparadox.arena_sandbox.failed", data.error()), true);
                yield false;
            }
            case REJECT_STALE_STATE -> {
                ArenaPlayerState.clearPending(player);
                player.displayClientMessage(Component.translatable(
                        "message.finalparadox.arena_sandbox.stale"), true);
                yield false;
            }
        };
    }

    private static boolean beginAndWait(
            ServerPlayer player,
            ServerLevel arena,
            ArenaSlots.ArenaSlot slot,
            ArenaDeploymentData data
    ) {
        if (!arena.isInWorldBounds(slot.definition().minimumCorner(slot.floorAnchor()))
                || !arena.isInWorldBounds(slot.definition().maximumCorner(slot.floorAnchor()))) {
            player.displayClientMessage(Component.translatable(
                    "message.finalparadox.arena_sandbox.out_of_bounds"), true);
            return false;
        }
        data.begin(slot.definition(), slot.floorAnchor());
        ArenaPlayerState.waitFor(player, slot);
        player.displayClientMessage(Component.translatable(
                "message.finalparadox.arena_sandbox.constructing", slot.definition().id().toUpperCase()), true);
        return true;
    }

    public static void tick(ServerLevel arena) {
        if (!ModDimensions.isActiveArena(arena.dimension())) return;
        for (ServerPlayer player : arena.getServer().getPlayerList().getPlayers()) {
            ArenaSlots.ArenaSlot slot = ArenaPlayerState.pending(player).orElse(null);
            if (slot == null) continue;
            if (!ArenaPlayerState.pendingOriginStillMatches(player)) {
                ArenaPlayerState.clearPending(player);
                continue;
            }
            ArenaDeploymentData data = ArenaDeploymentData.get(arena, slot.definition());
            ArenaSandboxTravelDecision.Decision decision = ArenaSandboxTravelDecision.decide(
                    data.state(), data.arenaId(), data.floorAnchor().orElse(null), slot);
            if (decision == ArenaSandboxTravelDecision.Decision.TELEPORT) {
                teleport(player, arena, slot);
            } else if (decision == ArenaSandboxTravelDecision.Decision.REJECT_ERROR) {
                ArenaPlayerState.clearPending(player);
                player.displayClientMessage(Component.translatable(
                        "message.finalparadox.arena_sandbox.failed", data.error()), true);
            } else if (decision == ArenaSandboxTravelDecision.Decision.REJECT_STALE_STATE) {
                ArenaPlayerState.clearPending(player);
                player.displayClientMessage(Component.translatable(
                        "message.finalparadox.arena_sandbox.stale"), true);
            }
        }
    }

    private static void teleport(
            ServerPlayer player,
            ServerLevel arena,
            ArenaSlots.ArenaSlot slot
    ) {
        BlockPos entry = slot.entryPosition();
        arena.getChunkAt(entry);
        ArenaPlayerState.clearPending(player);
        ArenaPlayerState.enter(player, slot);
        player.teleportTo(arena, entry.getX() + 0.5D, entry.getY(), entry.getZ() + 0.5D,
                slot.entryYaw(), player.getXRot());
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.fallDistance = 0.0F;
        arena.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable(
                "message.finalparadox.arena_sandbox.entered", slot.definition().id().toUpperCase()), true);
    }
}
