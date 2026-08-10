package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Persistent, arena-scoped fight rosters.
 *
 * <p>The source map used global {@code @a} selectors because only one party
 * occupied its boss world. The mod can host several deployed arenas in one
 * dimension, so death and defeat handling must instead use the players who
 * were physically inside this arena when its fight began.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class ArenaFightParticipants extends SavedData {
    private static final String DATA_NAME = "finalparadox_arena_fight_participants";
    private static final String FIGHTS = "Fights";
    private static final String PARTICIPANTS = "Participants";
    private static final String DEFEATED = "Defeated";
    private static final String UUID_KEY = "Uuid";

    private final Map<String, Roster> fights = new HashMap<>();

    private ArenaFightParticipants() {
    }

    public static ArenaFightParticipants get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ArenaFightParticipants::load, ArenaFightParticipants::new, DATA_NAME);
    }

    private static ArenaFightParticipants load(CompoundTag root) {
        ArenaFightParticipants data = new ArenaFightParticipants();
        CompoundTag fightsTag = root.getCompound(FIGHTS);
        for (String arenaId : fightsTag.getAllKeys()) {
            if (!fightsTag.contains(arenaId, Tag.TAG_COMPOUND)) continue;
            data.fights.put(arenaId, Roster.read(fightsTag.getCompound(arenaId)));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        CompoundTag fightsTag = new CompoundTag();
        fights.forEach((arenaId, roster) -> fightsTag.put(arenaId, roster.write()));
        root.put(FIGHTS, fightsTag);
        return root;
    }

    /** Captures the non-spectator players physically inside this arena. */
    public static void begin(
            ServerLevel level,
            ArenaDefinition definition,
            BlockPos anchor
    ) {
        AABB bounds = ArenaWaitingBatch.arenaBounds(definition, anchor);
        Set<UUID> participants = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && bounds.contains(player.position())) {
                participants.add(player.getUUID());
            }
        }
        ArenaFightParticipants data = get(level);
        data.fights.put(definition.id(), new Roster(participants, Set.of()));
        data.setDirty();
    }

    /**
     * Marks a death or departure only when the player belongs to this fight.
     * The return value is the ownership check used by death-event routing.
     */
    public static boolean markDefeated(
            ServerLevel level,
            ArenaDefinition definition,
            UUID playerId
    ) {
        ArenaFightParticipants data = get(level);
        Roster roster = data.fights.get(definition.id());
        if (roster == null || !roster.isParticipant(playerId)) return false;
        if (roster.markDefeated(playerId)) data.setDirty();
        return true;
    }

    public static boolean isParticipant(
            ServerLevel level,
            ArenaDefinition definition,
            UUID playerId
    ) {
        Roster roster = get(level).fights.get(definition.id());
        return roster != null && roster.isParticipant(playerId);
    }

    public static boolean isDefeated(
            ServerLevel level,
            ArenaDefinition definition,
            UUID playerId
    ) {
        Roster roster = get(level).fights.get(definition.id());
        return roster != null && roster.isDefeated(playerId);
    }

    public static boolean isInAnyFight(ServerLevel level, UUID playerId) {
        for (Roster roster : get(level).fights.values()) {
            if (roster.isParticipant(playerId)) return true;
        }
        return false;
    }

    public static boolean allDefeated(ServerLevel level, ArenaDefinition definition) {
        Roster roster = get(level).fights.get(definition.id());
        return roster != null && roster.allDefeated();
    }

    public static Set<UUID> participants(ServerLevel level, ArenaDefinition definition) {
        Roster roster = get(level).fights.get(definition.id());
        return roster == null ? Set.of() : Set.copyOf(roster.participants);
    }

    public static List<ServerPlayer> onlinePlayers(
            ServerLevel level,
            ArenaDefinition definition
    ) {
        Set<UUID> participants = participants(level, definition);
        return level.players().stream()
                .filter(player -> participants.contains(player.getUUID()))
                .toList();
    }

    /** Re-arms every participant when an encounter intentionally revives the party mid-fight. */
    public static void resetDefeated(ServerLevel level, ArenaDefinition definition) {
        ArenaFightParticipants data = get(level);
        Roster roster = data.fights.get(definition.id());
        if (roster != null && roster.resetDefeated()) data.setDirty();
    }

    public static void clear(ServerLevel level, ArenaDefinition definition) {
        ArenaFightParticipants data = get(level);
        if (data.fights.remove(definition.id()) != null) data.setDirty();
    }

    private static void markUnavailable(ServerLevel level, UUID playerId) {
        ArenaFightParticipants data = get(level);
        boolean changed = false;
        for (Roster roster : data.fights.values()) {
            if (roster.isParticipant(playerId)) {
                changed |= roster.markDefeated(playerId);
            }
        }
        if (changed) data.setDirty();
    }

    private static boolean defeatedInAnyFight(ServerPlayer player) {
        for (Roster roster : get(player.serverLevel()).fights.values()) {
            if (roster.isDefeated(player.getUUID())) return true;
        }
        return false;
    }

    private static void restoreEliminatedMode(ServerPlayer player) {
        if (ModDimensions.ARENA_DIMENSION.equals(player.serverLevel().dimension())
                && defeatedInAnyFight(player)
                && !player.isSpectator()) {
            player.setGameMode(GameType.SPECTATOR);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && ModDimensions.ARENA_DIMENSION.equals(player.serverLevel().dimension())) {
            markUnavailable(player.serverLevel(), player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restoreEliminatedMode(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ModDimensions.ARENA_DIMENSION.equals(event.getFrom())
                && !ModDimensions.ARENA_DIMENSION.equals(event.getTo())) {
            ServerLevel arena = player.getServer().getLevel(event.getFrom());
            if (arena != null) markUnavailable(arena, player.getUUID());
        } else if (ModDimensions.ARENA_DIMENSION.equals(event.getTo())) {
            restoreEliminatedMode(player);
        }
    }

    /** Pure roster policy kept package-private for unit tests. */
    static final class Roster {
        private final Set<UUID> participants;
        private final Set<UUID> defeated;

        Roster(Collection<UUID> participants, Collection<UUID> defeated) {
            this.participants = new HashSet<>(participants);
            this.defeated = new HashSet<>(defeated);
            this.defeated.retainAll(this.participants);
        }

        boolean isParticipant(UUID playerId) {
            return participants.contains(playerId);
        }

        boolean isDefeated(UUID playerId) {
            return defeated.contains(playerId);
        }

        boolean markDefeated(UUID playerId) {
            return participants.contains(playerId) && defeated.add(playerId);
        }

        boolean allDefeated() {
            return !participants.isEmpty() && defeated.containsAll(participants);
        }

        boolean resetDefeated() {
            if (defeated.isEmpty()) return false;
            defeated.clear();
            return true;
        }

        CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.put(PARTICIPANTS, writeUuids(participants));
            tag.put(DEFEATED, writeUuids(defeated));
            return tag;
        }

        static Roster read(CompoundTag tag) {
            return new Roster(
                    readUuids(tag.getList(PARTICIPANTS, Tag.TAG_COMPOUND)),
                    readUuids(tag.getList(DEFEATED, Tag.TAG_COMPOUND)));
        }

        private static ListTag writeUuids(Collection<UUID> values) {
            ListTag list = new ListTag();
            for (UUID value : values) {
                CompoundTag entry = new CompoundTag();
                entry.putUUID(UUID_KEY, value);
                list.add(entry);
            }
            return list;
        }

        private static Set<UUID> readUuids(ListTag list) {
            Set<UUID> values = new HashSet<>();
            for (int index = 0; index < list.size(); index++) {
                CompoundTag entry = list.getCompound(index);
                if (entry.hasUUID(UUID_KEY)) values.add(entry.getUUID(UUID_KEY));
            }
            return values;
        }
    }
}
