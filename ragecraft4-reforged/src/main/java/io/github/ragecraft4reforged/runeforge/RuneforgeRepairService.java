package io.github.ragecraft4reforged.runeforge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public final class RuneforgeRepairService {
    private static final int EXPERIENCE_PER_REPAIR_COST = 10;
    private static final List<Item> MODDED_MATERIAL_CANDIDATES = List.of(
            Items.LEATHER,
            Items.CHAIN,
            Items.COBBLESTONE,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.DIAMOND,
            Items.NETHERITE_INGOT,
            Items.OAK_PLANKS
    );

    public record Cost(Item material, int materialCount, int experiencePoints) {
        public boolean hasMaterialCost() {
            return material != Items.AIR && materialCount > 0;
        }

        public ItemStack materialStack() {
            return material.getDefaultInstance();
        }
    }

    public record Result(boolean success, Component message) {
    }

    public static Cost cost(ItemStack stack) {
        if (stack.isEmpty()) {
            return new Cost(Items.AIR, 0, 0);
        }

        int repairUnits = Math.max(0, RuneforgeData.getRepairCost(stack));
        int experience = repairUnits > Integer.MAX_VALUE / EXPERIENCE_PER_REPAIR_COST
                ? Integer.MAX_VALUE : repairUnits * EXPERIENCE_PER_REPAIR_COST;
        int amount = materialAmount(stack);
        Item material = amount > 0 ? repairMaterial(stack) : Items.AIR;
        if (material == Items.AIR) {
            amount = 0;
        }
        return new Cost(material, amount, experience);
    }

    public static ItemStack preview(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamaged()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.setDamageValue(0);
        return result;
    }

    public static Result repair(ServerPlayer player, RuneforgeWorkbenchBlockEntity workbench) {
        ItemStackHandler inventory = workbench.getInventory();
        ItemStack equipment = inventory.getStackInSlot(RuneforgeWorkbenchBlockEntity.BASE_SLOT);
        if (equipment.isEmpty()) {
            return fail("empty");
        }
        if (!RuneforgeItemRules.isSupportedBase(equipment)) {
            return fail("unsupported");
        }
        if (!equipment.isDamaged()) {
            return fail("not_damaged");
        }

        Cost cost = cost(equipment);
        if (!player.getAbilities().instabuild) {
            if (player.totalExperience < cost.experiencePoints()) {
                return new Result(false, Component.translatable(
                        "message.ragecraft4reforged.runeforge.repair_missing_experience",
                        cost.experiencePoints()));
            }
            if (cost.hasMaterialCost() && count(player, cost.material()) < cost.materialCount()) {
                return new Result(false, Component.translatable(
                        "message.ragecraft4reforged.runeforge.repair_missing_material",
                        cost.material().getDescription(), cost.materialCount()));
            }
        }

        if (!player.getAbilities().instabuild) {
            if (cost.hasMaterialCost()) {
                consume(player, cost.material(), cost.materialCount());
            }
            player.giveExperiencePoints(-cost.experiencePoints());
        }
        equipment.setDamageValue(0);
        inventory.setStackInSlot(RuneforgeWorkbenchBlockEntity.BASE_SLOT, equipment);
        workbench.setChanged();
        if (workbench.getLevel() != null) {
            workbench.getLevel().sendBlockUpdated(workbench.getBlockPos(), workbench.getBlockState(),
                    workbench.getBlockState(), 3);
        }
        playRepairEffects(player.serverLevel(), workbench.getBlockPos());

        Component message = cost.hasMaterialCost()
                ? Component.translatable("message.ragecraft4reforged.runeforge.repair_success",
                cost.experiencePoints(), cost.materialCount(), cost.material().getDescription())
                : Component.translatable("message.ragecraft4reforged.runeforge.repair_success_experience",
                cost.experiencePoints());
        return new Result(true, message);
    }

    private static int materialAmount(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return switch (armor.getType()) {
                case CHESTPLATE -> 4;
                case LEGGINGS -> 3;
                case HELMET, BOOTS -> 2;
            };
        }
        if (stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof PickaxeItem) {
            return 3;
        }
        return 0;
    }

    private static Item repairMaterial(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            if (armor.getMaterial() == ArmorMaterials.LEATHER) {
                return Items.LEATHER;
            }
            if (armor.getMaterial() == ArmorMaterials.CHAIN) {
                return Items.CHAIN;
            }
            if (armor.getMaterial() == ArmorMaterials.IRON) {
                return Items.IRON_INGOT;
            }
            if (armor.getMaterial() == ArmorMaterials.DIAMOND) {
                return Items.DIAMOND;
            }
            if (armor.getMaterial() == ArmorMaterials.NETHERITE) {
                return Items.NETHERITE_INGOT;
            }
            if (armor.getMaterial() == ArmorMaterials.GOLD) {
                return Items.AIR;
            }
        }
        if (stack.getItem() instanceof TieredItem tiered) {
            Tier tier = tiered.getTier();
            if (tier == Tiers.STONE) {
                return Items.COBBLESTONE;
            }
            if (tier == Tiers.IRON) {
                return Items.IRON_INGOT;
            }
            if (tier == Tiers.DIAMOND) {
                return Items.DIAMOND;
            }
            if (tier == Tiers.NETHERITE) {
                return Items.NETHERITE_INGOT;
            }
            if (tier == Tiers.WOOD || tier == Tiers.GOLD) {
                return Items.AIR;
            }
        }

        for (Item candidate : MODDED_MATERIAL_CANDIDATES) {
            if (stack.getItem().isValidRepairItem(stack, candidate.getDefaultInstance())) {
                return candidate;
            }
        }
        return Items.AIR;
    }

    private static int count(ServerPlayer player, Item wanted) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(wanted)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void consume(ServerPlayer player, Item wanted, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(wanted)) {
                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                remaining -= removed;
            }
        }
        player.getInventory().setChanged();
    }

    private static void playRepairEffects(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.5F);
        level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0F, 2.0F);
        level.sendParticles(ParticleTypes.WHITE_ASH,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                40, 0.25, 0.3, 0.25, 0.0);
        level.sendParticles(ParticleTypes.FLASH,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                1, 0.0, 0.0, 0.0, 0.0);
    }

    private static Result fail(String reason) {
        return new Result(false, Component.translatable(
                "message.ragecraft4reforged.runeforge.repair_" + reason));
    }

    private RuneforgeRepairService() {
    }
}
