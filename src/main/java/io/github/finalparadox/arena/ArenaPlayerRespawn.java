package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * Server-authoritative arena respawn override.
 *
 * <p>When a boss fight starts, every player inside the arena dimension gets a
 * forced spawnpoint at the current arena's shared respawn position, mirroring
 * the source map's {@code /spawnpoint @a ...} commands. The original respawn
 * (dimension, optional bed position, angle, forced flag) is snapshotted once
 * into the player's persistent NBT and restored only when the player actually
 * leaves the arena dimension, so deaths inside the arena keep respawning at
 * the arena even after the fight ends.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class ArenaPlayerRespawn {
    /** Mod-unique snapshot key stored under the player's persistent data. */
    public static final String SNAPSHOT_KEY = "finalparadox.arena_respawn";

    private static final String ACTIVE = "active";
    private static final String HAS_POSITION = "has_position";
    private static final String DIMENSION = "dimension";
    private static final String POSITION = "position";
    private static final String ANGLE = "angle";
    private static final String FORCED = "forced";
    private static final ResourceLocation OVERWORLD_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
    private static final ResourceLocation ARENA_ID =
            ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "arena_dimension");

    private ArenaPlayerRespawn() {
    }

    /**
     * Overrides the respawn point of every player currently inside {@code level}
     * with this arena's shared respawn. The first activation snapshots each
     * player's original respawn; later activations only refresh the arena
     * respawn and never touch the original snapshot.
     */
    public static void activateForArena(ServerLevel level, ArenaDefinition definition, BlockPos anchor) {
        ArenaDefinitions.respawnFor(definition).ifPresent(respawn -> {
            BlockPos destination = anchor.offset(respawn.offset());
            for (ServerPlayer player : ArenaFightParticipants.onlinePlayers(level, definition)) {
                activate(player, level.dimension(), destination, respawn.yaw());
            }
        });
    }

    private static void activate(
            ServerPlayer player,
            ResourceKey<Level> arenaDimension,
            BlockPos destination,
            float yaw
    ) {
        CompoundTag root = player.getPersistentData();
        firstOrExisting(root, snapshot(player));
        player.setRespawnPosition(arenaDimension, destination, yaw, true, false);
    }

    private static RespawnSnapshot snapshot(ServerPlayer player) {
        return new RespawnSnapshot(
                player.getRespawnDimension().location(),
                player.getRespawnPosition(),
                player.getRespawnAngle(),
                player.isRespawnForced());
    }

    /**
     * Restores the player's original respawn and atomically clears the snapshot.
     * A missing, inactive, or corrupt snapshot is a safe no-op that leaves the
     * current respawn untouched; an inactive residue is still cleaned up.
     */
    public static void restore(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        RespawnSnapshot snapshot = snapshotIn(root);
        if (snapshot == null) {
            if (root.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
                root.remove(SNAPSHOT_KEY);
            }
            return;
        }
        player.setRespawnPosition(
                ResourceKey.create(Registries.DIMENSION, snapshot.dimension()),
                snapshot.position(),
                snapshot.angle(),
                snapshot.forced(),
                false);
        root.remove(SNAPSHOT_KEY);
    }

    static boolean isArena(ResourceKey<Level> dimension) {
        return isArenaLocation(dimension.location());
    }

    static boolean isArenaLocation(ResourceLocation dimension) {
        return ARENA_ID.equals(dimension);
    }

    /** True when the player moved from the arena dimension to any other dimension. */
    static boolean leavesArena(ResourceKey<Level> from, ResourceKey<Level> to) {
        return isArena(from) && !isArena(to);
    }

    static boolean hasSnapshot(CompoundTag root) {
        return root.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND);
    }

    @Nullable
    static RespawnSnapshot snapshotIn(CompoundTag root) {
        if (!hasSnapshot(root)) return null;
        return RespawnSnapshot.read(root.getCompound(SNAPSHOT_KEY));
    }

    /**
     * Returns the existing snapshot when one is present; otherwise stores
     * {@code current} as the original respawn and returns it. This keeps the
     * original spawn across consecutive arena fights.
     */
    static RespawnSnapshot firstOrExisting(CompoundTag root, RespawnSnapshot current) {
        if (!hasSnapshot(root)) {
            root.put(SNAPSHOT_KEY, current.write());
            return current;
        }
        RespawnSnapshot existing = RespawnSnapshot.read(root.getCompound(SNAPSHOT_KEY));
        if (existing != null) return existing;
        root.put(SNAPSHOT_KEY, current.write());
        return current;
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (leavesArena(event.getFrom(), event.getTo())) {
            restore(player);
        }
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isArena(player.level().dimension())) {
            restore(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag source = event.getOriginal().getPersistentData();
        if (hasSnapshot(source)) {
            player.getPersistentData().put(SNAPSHOT_KEY, source.getCompound(SNAPSHOT_KEY).copy());
        }
    }

    /**
     * Immutable respawn snapshot with a lossless NBT codec. Package-private so
     * the codec and policies can be unit-tested without a ServerPlayer mock.
     */
    record RespawnSnapshot(
            ResourceLocation dimension,
            @Nullable BlockPos position,
            float angle,
            boolean forced) {

        CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean(ACTIVE, true);
            tag.putString(DIMENSION, dimension.toString());
            if (position != null) {
                tag.putBoolean(HAS_POSITION, true);
                tag.putIntArray(POSITION, new int[]{
                        position.getX(), position.getY(), position.getZ()});
            } else {
                tag.putBoolean(HAS_POSITION, false);
            }
            tag.putFloat(ANGLE, angle);
            tag.putBoolean(FORCED, forced);
            return tag;
        }

        @Nullable
        static RespawnSnapshot read(CompoundTag tag) {
            if (!tag.getBoolean(ACTIVE)) return null;
            ResourceLocation dimension = ResourceLocation.tryParse(tag.getString(DIMENSION));
            if (dimension == null) {
                dimension = OVERWORLD_ID;
            }
            BlockPos position = null;
            if (tag.getBoolean(HAS_POSITION)) {
                int[] raw = tag.getIntArray(POSITION);
                if (raw.length == 3) {
                    position = new BlockPos(raw[0], raw[1], raw[2]);
                }
            }
            return new RespawnSnapshot(dimension, position, tag.getFloat(ANGLE), tag.getBoolean(FORCED));
        }
    }
}
