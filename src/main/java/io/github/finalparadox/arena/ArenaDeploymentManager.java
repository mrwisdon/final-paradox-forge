package io.github.finalparadox.arena;

import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.EotharEchoEntity;
import io.github.finalparadox.entity.KoyomiBossEntity;
import io.github.finalparadox.entity.GariBossEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.entity.ZombieSupermatrixEntity;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArenaDeploymentManager {
    private static final int TICKS_PER_TILE = 5;

    private ArenaDeploymentManager() {
    }

    public static void tick(ServerLevel level) {
        if (level.getGameTime() % TICKS_PER_TILE != 0) return;
        ArenaDeploymentData data = ArenaDeploymentData.get(level);
        if (data.state() == ArenaDeploymentData.DeploymentState.READY
                && ArenaDefinitions.B5.id().equals(data.arenaId())
                && !io.github.finalparadox.entity.B5EncounterManager.isActive(level)
                && data.activeBossUuid().isEmpty()
                && data.stagedKoyoUuid().isEmpty()
                && data.stagedGariUuid().isEmpty()
                && data.korosUuid().isEmpty()
                && data.floorAnchor().isPresent()) {
            if (B5ArenaStaging.spawn(level, data, data.floorAnchor().orElseThrow()).isEmpty()) {
                data.fail("Could not migrate the staged B5 bosses and Echo of Koros");
                notifyPlayers(level, Component.literal("B5 arena migration failed while creating its waiting entities."));
            } else {
                notifyPlayers(level, Component.literal(
                        "B5 waiting entities restored. Talk to the Echo of Koros to begin the encounter."));
            }
            return;
        }
        if (data.state() == ArenaDeploymentData.DeploymentState.READY
                && ArenaDefinitions.B8.id().equals(data.arenaId())
                && !io.github.finalparadox.entity.B8EncounterManager.isActive(level)
                && !data.b8Triggered()
                && data.activeBossUuid().isEmpty()
                && data.korosUuid().isEmpty()
                && data.floorAnchor().isPresent()) {
            BlockPos anchor = data.floorAnchor().orElseThrow();
            BlockPos pos = anchor.offset(ArenaDefinitions.B8_KOROS_OFFSET);
            level.getChunkAt(pos);
            KorosEchoEntity koros = KorosEchoEntity.createB8(level, anchor);
            if (koros != null) {
                koros.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                if (level.addFreshEntity(koros)) {
                    data.setKorosUuid(koros.getUUID());
                    koros.playArrivalEffects();
                }
            }
            return;
        }
        if (data.state() == ArenaDeploymentData.DeploymentState.READY
                && ArenaDefinitions.MARAWTHAR.id().equals(data.arenaId())) {
            if (data.marawTharTriggered() || data.activeBossUuid().isPresent()) return;
            Optional<BlockPos> anchorResult = data.floorAnchor();
            if (anchorResult.isEmpty()) return;
            BlockPos anchor = anchorResult.get();
            data.eotharUuid().ifPresent(uuid -> {
                Entity existing = level.getEntity(uuid);
                if (!(existing instanceof EotharEchoEntity echo) || !echo.isAlive()) {
                    data.clearEothar();
                }
            });
            if (data.eotharUuid().isPresent()) {
                Entity existing = level.getEntity(data.eotharUuid().orElseThrow());
                if (existing instanceof EotharEchoEntity echo
                        && !MarawTharArenaStaging.anyPlayerNear(level, anchor, 30.0D)) {
                    echo.depart();
                    data.clearEothar();
                }
                return;
            }
            if (MarawTharArenaStaging.anyPlayerNear(level, anchor, 18.0D)) {
                MarawTharArenaStaging.spawnEothar(level, data, anchor).ifPresent(echo ->
                        notifyPlayers(level, Component.literal(
                                "Eothar's echo stirs at the Maraw'Thar arena.")));
            }
            return;
        }

        if (data.state() != ArenaDeploymentData.DeploymentState.DEPLOYING) return;

        Optional<ArenaDefinition> definitionResult = ArenaDefinitions.find(data.arenaId());
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (definitionResult.isEmpty() || anchorResult.isEmpty()) {
            data.fail("Deployment definition or floor anchor is missing");
            return;
        }

        ArenaDefinition definition = definitionResult.get();
        if (data.nextTile() >= definition.tileCount()) {
            data.fail("Saved tile index is outside the arena definition");
            return;
        }

        ArenaDefinition.ArenaTile tile = definition.tile(data.nextTile());
        BlockPos origin = definition.minimumCorner(anchorResult.get()).offset(tile.offset());
        Optional<StructureTemplate> templateResult = level.getStructureManager().get(tile.template());
        if (templateResult.isEmpty()) {
            data.fail("Missing structure template " + tile.template());
            notifyPlayers(level, Component.literal("Arena deployment failed: missing " + tile.template()));
            return;
        }

        StructureTemplate template = templateResult.get();
        if (!template.getSize().equals(tile.expectedSize())) {
            data.fail("Unexpected template size for " + tile.template());
            notifyPlayers(level, Component.literal("Arena deployment failed: wrong size for " + tile.template()));
            return;
        }

        List<ChunkPos> temporarilyForced = new ArrayList<>();
        boolean placed;
        try {
            forceTileChunks(level, origin, tile.expectedSize(), temporarilyForced);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setIgnoreEntities(true)
                    .setKeepLiquids(false);
            placed = template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);
        } catch (RuntimeException exception) {
            data.fail("Template placement threw " + exception.getClass().getSimpleName() + ": " + exception.getMessage());
            notifyPlayers(level, Component.literal("Arena deployment failed while placing " + tile.template()));
            return;
        } finally {
            for (ChunkPos chunk : temporarilyForced) level.setChunkForced(chunk.x, chunk.z, false);
        }

        if (!placed) {
            data.fail("Template placement returned false for " + tile.template());
            notifyPlayers(level, Component.literal("Arena deployment failed while placing " + tile.template()));
            return;
        }

        data.advance(definition);
        if (data.state() == ArenaDeploymentData.DeploymentState.READY) {
            if (definition == ArenaDefinitions.B5
                    && B5ArenaStaging.spawn(level, data, anchorResult.get()).isEmpty()) {
                data.fail("Could not create the staged B5 bosses and Echo of Koros");
                notifyPlayers(level, Component.literal("B5 arena deployment failed while creating its waiting entities."));
                return;
            }
            String nextStep = definition == ArenaDefinitions.B5
                    ? " Talk to the Echo of Koros to begin the encounter."
                    : " Run /finalparadox arena start " + definition.id() + " when ready.";
            notifyPlayers(level, Component.literal(definition.id().toUpperCase()
                    + " arena deployment complete at floor anchor " + format(anchorResult.get()) + "." + nextStep));
        }
    }

    private static void forceTileChunks(ServerLevel level, BlockPos origin, Vec3i size, List<ChunkPos> temporarilyForced) {
        ChunkPos min = new ChunkPos(origin);
        ChunkPos max = new ChunkPos(origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
        for (int x = min.x; x <= max.x; x++) {
            for (int z = min.z; z <= max.z; z++) {
                long key = ChunkPos.asLong(x, z);
                if (!level.getForcedChunks().contains(key) && level.setChunkForced(x, z, true)) {
                    temporarilyForced.add(new ChunkPos(x, z));
                }
                level.getChunk(x, z);
            }
        }
    }

    public static Optional<Entity> findActiveBoss(
            ServerLevel level,
            ArenaDeploymentData data,
            ArenaDefinition definition
    ) {
        Optional<BlockPos> anchorResult = data.floorAnchor();
        if (anchorResult.isEmpty()) return Optional.empty();
        BlockPos anchor = anchorResult.get();
        BlockPos spawn = definition.bossSpawnBlock(anchor);
        level.getChunkAt(spawn);
        level.getChunkAt(spawn.offset(16, 0, 0));
        if (definition.id().equals(ArenaDefinitions.B5.id())) {
            level.getChunkAt(anchor.offset(ArenaDefinitions.B5_KOYO_OFFSET));
            level.getChunkAt(anchor.offset(ArenaDefinitions.B5_GARI_OFFSET));
        }

        if (data.activeBossUuid().isPresent()) {
            Entity savedBoss = level.getEntity(data.activeBossUuid().get());
            if (isExpectedLivingBoss(savedBoss, definition)) {
                return Optional.of(savedBoss);
            }
            data.clearActiveBoss();
        }

        AABB arenaBounds = new AABB(definition.minimumCorner(anchor), definition.maximumCorner(anchor).offset(1, 1, 1));
        if (definition.id().equals(ArenaDefinitions.B8.id())) {
            // The matrix core floats seven blocks above the floor anchor.
            arenaBounds = arenaBounds.expandTowards(0.0D, 12.0D, 0.0D);
        }
        Entity boss;
        if (definition.id().equals(ArenaDefinitions.MARAWTHAR.id())) {
            List<MarawTharBossEntity> bosses =
                    level.getEntities(ModEntities.MARAWTHAR.get(), arenaBounds, MarawTharBossEntity::isAlive);
            if (bosses.isEmpty()) return Optional.empty();
            boss = bosses.get(0);
        } else if (definition.id().equals(ArenaDefinitions.B5.id())) {
            List<KoyomiBossEntity> koyos =
                    level.getEntities(ModEntities.KOYOMI.get(), arenaBounds, KoyomiBossEntity::isAlive);
            if (!koyos.isEmpty()) boss = koyos.get(0);
            else {
                List<GariBossEntity> garis =
                        level.getEntities(ModEntities.GARI.get(), arenaBounds, GariBossEntity::isAlive);
                if (garis.isEmpty()) return Optional.empty();
                boss = garis.get(0);
            }
        } else if (definition.id().equals(ArenaDefinitions.B8.id())) {
            List<ZombieSupermatrixEntity> matrices =
                    level.getEntities(ModEntities.ZOMBIE_SUPERMATRIX.get(), arenaBounds, ZombieSupermatrixEntity::isAlive);
            if (matrices.isEmpty()) return Optional.empty();
            boss = matrices.get(0);
        } else {
            List<ApigloBossEntity> bosses =
                    level.getEntities(ModEntities.APIGLO.get(), arenaBounds, ApigloBossEntity::isAlive);
            if (bosses.isEmpty()) return Optional.empty();
            boss = bosses.get(0);
        }
        data.setActiveBossUuid(boss.getUUID());
        return Optional.of(boss);
    }

    private static boolean isExpectedLivingBoss(Entity entity, ArenaDefinition definition) {
        if (entity == null || !entity.isAlive()) return false;
        if (definition.id().equals(ArenaDefinitions.MARAWTHAR.id())) return entity instanceof MarawTharBossEntity;
        if (definition.id().equals(ArenaDefinitions.B5.id())) {
            return entity instanceof KoyomiBossEntity || entity instanceof GariBossEntity;
        }
        if (definition.id().equals(ArenaDefinitions.B8.id())) {
            return entity instanceof ZombieSupermatrixEntity;
        }
        return entity instanceof ApigloBossEntity;
    }

    public static String format(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static void notifyPlayers(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) player.sendSystemMessage(message);
    }
}
