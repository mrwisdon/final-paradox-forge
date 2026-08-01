package io.github.ragecraft4reforged.content;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;

/** Server-authoritative bridge between native potion items and the bundled original-map functions. */
public final class InfinitePotionRuntime {
    private static final String POTION_COOLDOWN = "potion_cd";
    private static final String POTION_SELECTED = "potion_selected";
    private static final String DEADLY_SHRAPNEL = "R4RDeadlyShrapnel";
    private static final String OBSIDIAN_SHARDS = "R4RObsidianShards";
    private static final int USE_COOLDOWN = 100;
    private static final int REFILL_GUARD_PER_BOTTLE = 20;

    public static ItemStack emptyStack(InfinitePotionDefinition definition) {
        EmptyInfinitePotionItem item = (EmptyInfinitePotionItem) (definition.drinkable()
                ? ModItems.EMPTY_INFINITE_ELIXIR.get() : ModItems.EMPTY_INFINITE_POTION.get());
        return item.forType(definition.potionType());
    }

    public static void markThrown(ThrownPotion projectile, ServerPlayer player,
                                  InfinitePotionDefinition definition) {
        projectile.getPersistentData().putBoolean(DEADLY_SHRAPNEL,
                AccessoryCuriosIntegration.hasAbility(player, "deadly_shrapnel"));
        projectile.getPersistentData().putBoolean(OBSIDIAN_SHARDS,
                AccessoryCuriosIntegration.hasAbility(player, "obsidian_shards"));
        if (AccessoryCuriosIntegration.hasAbility(player, "call_void")) {
            execute(player, new ResourceLocation("skills", "offhand/call_void_potion"));
        }
        setScore(player, POTION_SELECTED, definition.potionType());
    }

    public static void activateThrown(ThrownPotion projectile, InfinitePotionDefinition definition) {
        if (projectile.getPersistentData().getBoolean(DEADLY_SHRAPNEL)) {
            execute(projectile, new ResourceLocation("skills", "offhand/deadly_shrapnel"));
        }
        if (projectile.getPersistentData().getBoolean(OBSIDIAN_SHARDS)) {
            execute(projectile, new ResourceLocation("skills", "offhand/obsidian_shards"));
        }
        execute(projectile, new ResourceLocation("skills",
                "potions/potion_" + definition.triggerIndex() + "_trigger"));
    }

    public static void activateDrink(ServerPlayer player, InfinitePotionDefinition definition) {
        setScore(player, POTION_SELECTED, definition.potionType());
        execute(player, new ResourceLocation(Ragecraft4Reforged.MOD_ID,
                "potions/drink_" + definition.potionType()));
    }

    public static void beginCooldown(ServerPlayer player, InfinitePotionDefinition definition) {
        setScore(player, POTION_SELECTED, definition.potionType());
        Objective objective = player.getScoreboard().getObjective(POTION_COOLDOWN);
        if (objective != null) {
            Score score = player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), objective);
            score.setScore(score.getScore() + USE_COOLDOWN);
        }
    }

    public static void refillIfReady(ServerPlayer player) {
        Objective objective = player.getScoreboard().getObjective(POTION_COOLDOWN);
        if (objective == null) {
            return;
        }
        Score score = player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), objective);
        if (score.getScore() != 0) {
            return;
        }

        int restored = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof EmptyInfinitePotionItem)) {
                continue;
            }
            InfinitePotionDefinition definition = EmptyInfinitePotionItem.definition(stack);
            if (definition == null) {
                continue;
            }
            ItemStack replacement = ModItems.infinitePotion(definition.potionType()).getDefaultInstance();
            player.getInventory().setItem(slot, replacement);
            restored++;
        }
        if (restored > 0) {
            score.setScore(restored * REFILL_GUARD_PER_BOTTLE);
            player.getInventory().setChanged();
        }
    }

    private static void setScore(ServerPlayer player, String objectiveName, int value) {
        Objective objective = player.getScoreboard().getObjective(objectiveName);
        if (objective != null) {
            player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), objective).setScore(value);
        }
    }

    private static void execute(net.minecraft.world.entity.Entity entity, ResourceLocation id) {
        if (entity.getServer() == null) {
            return;
        }
        entity.getServer().getFunctions().get(id).ifPresent(function -> entity.getServer().getFunctions().execute(
                function, entity.createCommandSourceStack().withPermission(2).withSuppressedOutput()));
    }

    private InfinitePotionRuntime() {
    }
}
