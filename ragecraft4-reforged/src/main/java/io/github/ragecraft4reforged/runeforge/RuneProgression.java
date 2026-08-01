package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ragecraft4Reforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RuneProgression {
    public static final int MAX_POWER = 12;

    private static final String ROOT = "RC4RuneProgression";
    private static final String POWER = "RunePower";

    public enum UnlockResult {
        SUCCESS,
        MAXIMUM,
        MISSING_EMERALD_BLOCK
    }

    public static int get(Player player) {
        return Math.max(0, Math.min(MAX_POWER,
                player.getPersistentData().getCompound(ROOT).getInt(POWER)));
    }

    public static UnlockResult unlockNext(ServerPlayer player) {
        int current = get(player);
        if (current >= MAX_POWER) {
            return UnlockResult.MAXIMUM;
        }
        if (!player.getAbilities().instabuild && !consumeEmeraldBlock(player)) {
            return UnlockResult.MISSING_EMERALD_BLOCK;
        }
        CompoundTag progression = player.getPersistentData().getCompound(ROOT);
        progression.putInt(POWER, current + 1);
        player.getPersistentData().put(ROOT, progression);
        player.getInventory().setChanged();
        return UnlockResult.SUCCESS;
    }

    public static Component resultMessage(UnlockResult result, int power) {
        return switch (result) {
            case SUCCESS -> Component.translatable(
                    "message.ragecraft4reforged.rune_power.unlocked", power, MAX_POWER);
            case MAXIMUM -> Component.translatable("message.ragecraft4reforged.rune_power.maximum");
            case MISSING_EMERALD_BLOCK -> Component.translatable(
                    "message.ragecraft4reforged.rune_power.emerald_block");
        };
    }

    private static boolean consumeEmeraldBlock(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.EMERALD_BLOCK)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        if (original.contains(ROOT)) {
            event.getEntity().getPersistentData().put(ROOT, original.getCompound(ROOT).copy());
        }
    }

    private RuneProgression() {
    }
}
