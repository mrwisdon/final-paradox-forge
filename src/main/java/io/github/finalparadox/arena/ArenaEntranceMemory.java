package io.github.finalparadox.arena;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

/**
 * Per-player record of boss entrances already shown. The list persists in the
 * player's NBT so a reset or a new arena deployment does not replay an
 * entrance the player has already seen.
 */
public final class ArenaEntranceMemory {
    private static final String KEY = "finalparadox.seen_arena_entrances";

    private ArenaEntranceMemory() {
    }

    public static boolean hasSeen(ServerPlayer player, String arenaId) {
        ListTag list = list(player);
        for (int index = 0; index < list.size(); index++) {
            if (arenaId.equals(list.getString(index))) return true;
        }
        return false;
    }

    /** Marks the arena as seen and returns true only on the player's first time. */
    public static boolean markIfFirst(ServerPlayer player, String arenaId) {
        if (hasSeen(player, arenaId)) return false;
        list(player).add(StringTag.valueOf(arenaId));
        return true;
    }

    private static ListTag list(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(KEY, Tag.TAG_LIST)) {
            tag.put(KEY, new ListTag());
        }
        return tag.getList(KEY, Tag.TAG_STRING);
    }
}
