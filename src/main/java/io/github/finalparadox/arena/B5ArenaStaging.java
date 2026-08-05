package io.github.finalparadox.arena;

import io.github.finalparadox.entity.B5EncounterManager;
import io.github.finalparadox.entity.B5EncounterData;
import io.github.finalparadox.entity.GariBossEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.entity.KoyomiBossEntity;
import io.github.finalparadox.item.ArenaCompassDestination;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/** Creates and owns the three waiting entities used by the original B5 introduction. */
public final class B5ArenaStaging {
    private B5ArenaStaging() {
    }

    public static Optional<Stage> spawn(ServerLevel level, ArenaDeploymentData data, BlockPos anchor) {
        if (B5EncounterManager.isActive(level)) return Optional.empty();
        cleanupWaiting(level, data);
        B5EncounterData.get(level).resetPreBattleDialogue();

        KoyomiBossEntity koyo = KoyomiBossEntity.createPrepared(level);
        GariBossEntity gari = GariBossEntity.createPrepared(level);
        KorosEchoEntity koros = KorosEchoEntity.create(level, anchor);
        if (koyo == null || gari == null || koros == null) return Optional.empty();

        BlockPos koyoPos = anchor.offset(ArenaDefinitions.B5_KOYO_OFFSET);
        BlockPos gariPos = anchor.offset(ArenaDefinitions.B5_GARI_OFFSET);
        BlockPos korosPos = anchor.offset(ArenaDefinitions.B5_KOROS_HITBOX_OFFSET);
        koyo.moveTo(koyoPos.getX(), koyoPos.getY(), koyoPos.getZ(), -90.0F, 0.0F);
        gari.moveTo(gariPos.getX(), gariPos.getY(), gariPos.getZ(), -90.0F, 0.0F);
        koros.moveTo(korosPos.getX(), korosPos.getY(), korosPos.getZ(), 0.0F, 0.0F);
        koyo.setArenaAnchor(anchor);
        hold(koyo, gari);

        if (!level.addFreshEntity(koyo)) return Optional.empty();
        if (!level.addFreshEntity(gari)) {
            koyo.discard();
            return Optional.empty();
        }
        if (!level.addFreshEntity(koros)) {
            koyo.discard();
            gari.discard();
            return Optional.empty();
        }

        data.setB5Stage(koyo.getUUID(), gari.getUUID(), koros.getUUID());
        koros.playArrivalEffects();
        return Optional.of(new Stage(koyo, gari, koros));
    }

    public static Optional<Stage> find(ServerLevel level, ArenaDeploymentData data) {
        data.floorAnchor().ifPresent(anchor -> loadStageChunks(level, anchor));
        KoyomiBossEntity koyo = entity(level, data.stagedKoyoUuid(), KoyomiBossEntity.class);
        GariBossEntity gari = entity(level, data.stagedGariUuid(), GariBossEntity.class);
        KorosEchoEntity koros = entity(level, data.korosUuid(), KorosEchoEntity.class);
        return koyo == null || gari == null || koros == null
                ? Optional.empty() : Optional.of(new Stage(koyo, gari, koros));
    }

    public static boolean beginEncounter(ServerPlayer initiator) {
        ServerLevel level = initiator.serverLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level, ArenaDefinitions.B5);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !ArenaDefinitions.B5.id().equals(data.arenaId())
                || data.floorAnchor().isEmpty()
                || B5EncounterManager.isActive(level)) return false;

        Optional<Stage> result = find(level, data);
        if (result.isEmpty()) return false;
        BlockPos anchor = data.floorAnchor().orElseThrow();
        if (!allPlayersInside(level, anchor)) return false;

        B5EncounterData encounter = B5EncounterData.get(level);
        if (!encounter.preBattleDialoguePlayed() || encounter.preBattleDialogueTicks() >= 0) return false;

        Stage stage = result.get();
        hold(stage.koyo(), stage.gari());
        stage.koros().depart();
        data.clearKoros();
        B5EncounterManager.beginCountdown(level, anchor, stage.koyo(), stage.gari());
        ArenaCompassDestination.setForArena(level, ArenaDefinitions.B5, anchor);
        return true;
    }

    public static void tryStartPreBattleDialogue(
            ServerLevel level,
            ArenaDeploymentData data,
            BlockPos anchor
    ) {
        if (B5EncounterManager.isActive(level)) return;
        if (find(level, data).isEmpty()) return;
        B5EncounterData encounter = B5EncounterData.get(level);
        if (!encounter.preBattleDialoguePlayed() && anyPlayerInside(level, anchor)) {
            encounter.startPreBattleDialogue();
        }
    }

    public static boolean anyPlayerInside(ServerLevel level, BlockPos anchor) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() == level && !player.isSpectator() && inside(anchor, player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean allPlayersInside(ServerLevel level, BlockPos anchor) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level || !inside(anchor, player)) {
                return false;
            }
        }
        return true;
    }

    private static boolean inside(BlockPos anchor, ServerPlayer player) {
        return player.getX() >= anchor.getX() - 51.0D
                && player.getX() <= anchor.getX() + 7.0D
                && player.getY() >= anchor.getY() + 1.0D
                && player.getY() <= anchor.getY() + 11.0D
                && player.getZ() >= anchor.getZ() - 19.0D
                && player.getZ() <= anchor.getZ() + 19.0D;
    }

    public static void cleanupWaiting(ServerLevel level, ArenaDeploymentData data) {
        if (B5EncounterManager.isActive(level)) return;
        data.floorAnchor().ifPresent(anchor -> loadStageChunks(level, anchor));
        discard(level, data.stagedKoyoUuid());
        discard(level, data.stagedGariUuid());
        discard(level, data.korosUuid());
        data.clearB5Stage();
    }

    private static void loadStageChunks(ServerLevel level, BlockPos anchor) {
        level.getChunkAt(anchor.offset(ArenaDefinitions.B5_KOYO_OFFSET));
        level.getChunkAt(anchor.offset(ArenaDefinitions.B5_GARI_OFFSET));
        level.getChunkAt(anchor.offset(ArenaDefinitions.B5_KOROS_HITBOX_OFFSET));
    }

    private static void hold(KoyomiBossEntity koyo, GariBossEntity gari) {
        koyo.setInvulnerable(true);
        koyo.setNoAi(true);
        koyo.setBossBarEnabled(false);
        gari.setInvulnerable(true);
        gari.setNoAi(true);
        gari.setBossBarEnabled(false);
    }

    private static void discard(ServerLevel level, Optional<UUID> uuid) {
        uuid.map(level::getEntity).ifPresent(Entity::discard);
    }

    private static <T extends Entity> T entity(ServerLevel level, Optional<UUID> uuid, Class<T> type) {
        if (uuid.isEmpty()) return null;
        Entity entity = level.getEntity(uuid.get());
        return type.isInstance(entity) && entity.isAlive() ? type.cast(entity) : null;
    }

    public record Stage(KoyomiBossEntity koyo, GariBossEntity gari, KorosEchoEntity koros) {
    }
}
