package io.github.ragecraft4reforged.content;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.registry.ModItems;
import io.github.ragecraft4reforged.runeforge.RuneCatalog;
import io.github.ragecraft4reforged.runeforge.RuneCategory;
import io.github.ragecraft4reforged.runeforge.RuneDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * Adds optional RC4 accessory and upgrade-rune rolls to naturally generated
 * exploration loot.
 *
 * <p>Village profession chests, bonus chests, and generic containers are
 * intentionally excluded so accessories remain exploration rewards.</p>
 */
@Mod.EventBusSubscriber(modid = Ragecraft4Reforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WorldRewardLootEvents {
    private static final float EXPLORATION_CHEST_CHANCE = 0.08F;
    private static final float HIGH_VALUE_CHEST_CHANCE = 0.16F;
    private static final float LOW_UPGRADE_RUNE_CHANCE = 0.20F;
    private static final float MID_UPGRADE_RUNE_CHANCE = 0.15F;
    private static final float HIGH_UPGRADE_RUNE_CHANCE = 0.10F;

    private static final Set<ResourceLocation> EXPLORATION_CHESTS = Set.of(
            BuiltInLootTables.SIMPLE_DUNGEON,
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.SHIPWRECK_TREASURE,
            BuiltInLootTables.BURIED_TREASURE,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.JUNGLE_TEMPLE,
            BuiltInLootTables.PILLAGER_OUTPOST,
            BuiltInLootTables.UNDERWATER_RUIN_BIG,
            BuiltInLootTables.RUINED_PORTAL
    );

    private static final Set<ResourceLocation> MID_VALUE_CHESTS = Set.of(
            BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.STRONGHOLD_CROSSING,
            BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.NETHER_BRIDGE,
            BuiltInLootTables.WOODLAND_MANSION
    );

    private static final Set<ResourceLocation> HIGH_VALUE_CHESTS = Set.of(
            BuiltInLootTables.BASTION_TREASURE,
            BuiltInLootTables.ANCIENT_CITY,
            BuiltInLootTables.END_CITY_TREASURE
    );

    private WorldRewardLootEvents() {
    }

    @SubscribeEvent
    public static void addRc4RewardsToChests(LootTableLoadEvent event) {
        float accessoryChance = accessoryChance(event.getName());
        if (accessoryChance > 0.0F) {
            addAccessoryPool(event, accessoryChance);
        }

        UpgradeRuneTier upgradeTier = upgradeRuneTier(event.getName());
        if (upgradeTier != null) {
            addUpgradeRunePool(event, upgradeTier);
        }
    }

    private static void addAccessoryPool(LootTableLoadEvent event, float chance) {
        LootPool.Builder pool = LootPool.lootPool()
                .name(Ragecraft4Reforged.MOD_ID + "_accessory")
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(chance));

        ModItems.ALL_ACCESSORIES.forEach(accessory ->
                pool.add(LootItem.lootTableItem(accessory.get()).setWeight(1)));

        event.getTable().addPool(pool.build());
    }

    private static void addUpgradeRunePool(LootTableLoadEvent event, UpgradeRuneTier tier) {
        LootPool.Builder pool = LootPool.lootPool()
                .name(Ragecraft4Reforged.MOD_ID + "_upgrade_rune")
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(tier.chance));

        RuneCatalog.all().stream()
                .filter(definition -> definition.category() == RuneCategory.UPGRADE)
                .filter(tier::contains)
                .map(RuneDefinition::registryName)
                .map(ModItems::runeItem)
                .forEach(rune -> pool.add(LootItem.lootTableItem(rune).setWeight(1)));

        event.getTable().addPool(pool.build());
    }

    private static float accessoryChance(ResourceLocation lootTable) {
        if (MID_VALUE_CHESTS.contains(lootTable) || HIGH_VALUE_CHESTS.contains(lootTable)) {
            return HIGH_VALUE_CHEST_CHANCE;
        }
        if (EXPLORATION_CHESTS.contains(lootTable)) {
            return EXPLORATION_CHEST_CHANCE;
        }
        return 0.0F;
    }

    private static UpgradeRuneTier upgradeRuneTier(ResourceLocation lootTable) {
        if (HIGH_VALUE_CHESTS.contains(lootTable)) {
            return UpgradeRuneTier.HIGH;
        }
        if (MID_VALUE_CHESTS.contains(lootTable)) {
            return UpgradeRuneTier.MID;
        }
        if (EXPLORATION_CHESTS.contains(lootTable)) {
            return UpgradeRuneTier.LOW;
        }
        return null;
    }

    private enum UpgradeRuneTier {
        LOW(LOW_UPGRADE_RUNE_CHANCE, 1, 1),
        MID(MID_UPGRADE_RUNE_CHANCE, 3, 4),
        HIGH(HIGH_UPGRADE_RUNE_CHANCE, 5, 7);

        private final float chance;
        private final int minimumPower;
        private final int maximumPower;

        UpgradeRuneTier(float chance, int minimumPower, int maximumPower) {
            this.chance = chance;
            this.minimumPower = minimumPower;
            this.maximumPower = maximumPower;
        }

        private boolean contains(RuneDefinition definition) {
            return definition.runePower() >= minimumPower && definition.runePower() <= maximumPower;
        }
    }
}
