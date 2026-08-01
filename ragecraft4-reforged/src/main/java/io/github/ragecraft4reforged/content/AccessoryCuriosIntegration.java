package io.github.ragecraft4reforged.content;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;
import java.util.Set;

/** Bridges the dedicated Curios slot to the original map's item-NBT skill predicates. */
public final class AccessoryCuriosIntegration {
    public static final String SLOT_ID = "rc4_accessory";
    private static final String ACTIVE_TAG_PREFIX = "r4r_accessory_";
    private static final Set<String> ALL_ABILITY_TAGS = Set.of(
            "granite_skin", "toxicology", "determination", "spell_shield",
            "deadly_shrapnel", "supercharged", "eternal_flame", "blight_orb",
            "nature_blessing", "divine_bulwark", "obsidian_shards", "annihilating_curse",
            "last_stand", "dis_flammability", "avatar_fire", "shadow_spikes",
            "call_void", "summon_fangs", "cold_as_ice");

    public static Optional<ItemStack> equippedAccessory(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(handler -> handler.findCurio(SLOT_ID, 0))
                .map(result -> result.stack())
                .filter(stack -> stack.getItem() instanceof AccessoryItem);
    }

    public static boolean hasAbility(LivingEntity entity, String abilityTag) {
        return equippedAccessory(entity)
                .filter(stack -> ((AccessoryItem) stack.getItem()).abilityTags().contains(abilityTag))
                .isPresent();
    }

    public static void syncAbilityTags(ServerPlayer player) {
        ALL_ABILITY_TAGS.forEach(tag -> player.removeTag(ACTIVE_TAG_PREFIX + tag));
        equippedAccessory(player).ifPresent(stack -> {
            AccessoryItem accessory = (AccessoryItem) stack.getItem();
            accessory.abilityTags().forEach(tag -> player.addTag(ACTIVE_TAG_PREFIX + tag));
        });
    }

    private AccessoryCuriosIntegration() {
    }
}
