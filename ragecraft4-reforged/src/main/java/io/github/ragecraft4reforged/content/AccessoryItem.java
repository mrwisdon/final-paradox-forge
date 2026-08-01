package io.github.ragecraft4reforged.content;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/** An original-map accessory equipped in the dedicated RC4 Curios slot. */
public final class AccessoryItem extends Item implements ICurioItem {
    private static final String ETERNAL_FLAME = "eternal_flame";
    private static final int CELESTIAL_TORCH_INTERVAL = 10;
    private static final int CELESTIAL_TORCH_LIGHT_LEVEL = 15;

    private final List<String> abilityTags;
    private final String originalNameKey;
    private final List<String> originalLoreKeys;
    private final String headingSuffixKey;
    private final List<Modifier> modifiers;

    public AccessoryItem(List<String> abilityTags, String originalNameKey, List<String> originalLoreKeys,
                         String headingSuffixKey, List<Modifier> modifiers) {
        super(new Item.Properties().stacksTo(1));
        this.abilityTags = List.copyOf(abilityTags);
        this.originalNameKey = originalNameKey;
        this.originalLoreKeys = List.copyOf(originalLoreKeys);
        this.headingSuffixKey = headingSuffixKey;
        this.modifiers = List.copyOf(modifiers);
    }

    public String abilityTag() {
        return abilityTags.get(0);
    }

    public List<String> abilityTags() {
        return abilityTags;
    }

    public List<Modifier> modifiers() {
        return modifiers;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepare(stack);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(originalNameKey)
                .withStyle(style -> style.withColor(0xFF3369).withBold(true).withItalic(false));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ragecraft4reforged.accessory.slot")
                .withStyle(style -> style.withColor(0x555555).withItalic(false)));
        tooltip.add(Component.empty());
        for (int index = 0; index < originalLoreKeys.size(); index++) {
            boolean heading = index == 0;
            int color = heading ? 0xFF6987 : 0xFF878B;
            String key = originalLoreKeys.get(index);
            if (heading) {
                MutableComponent line = Component.translatable(key)
                        .withStyle(style -> style.withColor(color).withBold(true).withItalic(false))
                        .append(Component.translatable(headingSuffixKey)
                                .withStyle(style -> style.withColor(0xFF878B).withBold(false).withItalic(false)));
                tooltip.add(line);
            } else {
                tooltip.add(Component.translatable(key)
                        .withStyle(style -> style.withColor(color).withItalic(false)));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            prepare(stack);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!slotContext.entity().level().isClientSide) {
            prepare(stack);
            if (AccessoryCuriosIntegration.SLOT_ID.equals(slotContext.identifier())
                    && abilityTags.contains(ETERNAL_FLAME)
                    && slotContext.entity() instanceof ServerPlayer player
                    && player.tickCount % CELESTIAL_TORCH_INTERVAL == 0) {
                placeCelestialTorchLight(player);
            }
        }
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return AccessoryCuriosIntegration.SLOT_ID.equals(slotContext.identifier());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, UUID slotUuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> result = HashMultimap.create();
        if (!AccessoryCuriosIntegration.SLOT_ID.equals(slotContext.identifier())) {
            return result;
        }
        for (int index = 0; index < modifiers.size(); index++) {
            Modifier modifier = modifiers.get(index);
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(modifier.attribute());
            if (attribute != null) {
                String identity = slotUuid + ":" + abilityTag() + ":" + modifier.attribute() + ":" + index;
                UUID modifierUuid = UUID.nameUUIDFromBytes(identity.getBytes(StandardCharsets.UTF_8));
                result.put(attribute, new AttributeModifier(modifierUuid, "Ragecraft4 accessory",
                        modifier.amount(), modifier.operation()));
            }
        }
        return result;
    }

    private void prepare(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        abilityTags.forEach(abilityTag -> tag.putBoolean(abilityTag, true));
    }

    private static void placeCelestialTorchLight(ServerPlayer player) {
        Level level = player.level();
        BlockPos lightPosition = BlockPos.containing(player.getX(), player.getY() + 1.0, player.getZ());
        if (level.isInWorldBounds(lightPosition) && level.getBlockState(lightPosition).isAir()) {
            level.setBlock(lightPosition, Blocks.LIGHT.defaultBlockState()
                    .setValue(LightBlock.LEVEL, CELESTIAL_TORCH_LIGHT_LEVEL), Block.UPDATE_ALL);
        }
    }

    public record Modifier(ResourceLocation attribute, double amount, AttributeModifier.Operation operation) {
    }
}
