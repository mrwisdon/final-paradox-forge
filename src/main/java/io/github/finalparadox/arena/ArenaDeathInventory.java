package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModDimensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Keeps an arena participant's exact vanilla inventory across a boss death. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID)
public final class ArenaDeathInventory {
    static final String SNAPSHOT_KEY = "finalparadox.arena_death_inventory";
    private static final String INVENTORY = "Inventory";
    private static final String SELECTED = "Selected";

    private ArenaDeathInventory() {
    }

    /**
     * Runs after cancellable death-prevention handlers, but before vanilla
     * empties the inventory into item entities.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!shouldProtect(
                ModDimensions.isActiveArena(player.serverLevel().dimension()),
                ArenaFightParticipants.isInAnyFight(player.serverLevel(), player.getUUID()))) {
            return;
        }
        CompoundTag snapshot = new CompoundTag();
        snapshot.put(INVENTORY, player.getInventory().save(new ListTag()));
        snapshot.putInt(SELECTED, player.getInventory().selected);
        player.getPersistentData().put(SNAPSHOT_KEY, snapshot);
    }

    /** The saved inventory is restored on clone, so spawning its drops would duplicate it. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.getPersistentData().contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag source = event.getOriginal().getPersistentData();
        if (!source.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) return;
        CompoundTag snapshot = source.getCompound(SNAPSHOT_KEY);
        player.getInventory().load(snapshot.getList(INVENTORY, Tag.TAG_COMPOUND));
        player.getInventory().selected = Math.max(0, Math.min(8, snapshot.getInt(SELECTED)));
        player.getInventory().setChanged();
        source.remove(SNAPSHOT_KEY);
        player.getPersistentData().remove(SNAPSHOT_KEY);
    }

    static boolean shouldProtect(boolean inArenaDimension, boolean rosterMember) {
        return inArenaDimension && rosterMember;
    }
}
