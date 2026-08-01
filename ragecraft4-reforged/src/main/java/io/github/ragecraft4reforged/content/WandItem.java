package io.github.ragecraft4reforged.content;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Native item wrapper around Ragecraft IV's original spell function runtime. */
public final class WandItem extends Item {
    public static final String CHARGED = "R4RCharged";
    private static final ResourceLocation CAST_AFTER =
            new ResourceLocation(Ragecraft4Reforged.MOD_ID, "wand_cast_after");

    private final int spellId;
    private final List<String> originalNameKeys;
    private final List<String> originalLoreKeys;
    private final List<Modifier> modifiers;
    private final List<EnchantmentEntry> enchantments;
    private final List<String> abilityTags;

    public WandItem(int spellId, List<String> originalNameKeys, List<String> originalLoreKeys,
                    List<Modifier> modifiers, List<EnchantmentEntry> enchantments, List<String> abilityTags) {
        super(new Item.Properties().stacksTo(1));
        this.spellId = spellId;
        this.originalNameKeys = List.copyOf(originalNameKeys);
        this.originalLoreKeys = List.copyOf(originalLoreKeys);
        this.modifiers = List.copyOf(modifiers);
        this.enchantments = List.copyOf(enchantments);
        this.abilityTags = List.copyOf(abilityTags);
    }

    public int spellId() {
        return spellId;
    }

    public List<Modifier> modifiers() {
        return modifiers;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepare(stack, true);
        return stack;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            prepare(stack, isCharged(serverPlayer));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent name = Component.empty();
        for (int index = 0; index < originalNameKeys.size(); index++) {
            int color = originalNameKeys.size() == 1 || index > 0 ? 0x5ED3DB : 0x448FDB;
            name.append(Component.translatable(originalNameKeys.get(index))
                    .withStyle(style -> style.withColor(color).withBold(true).withItalic(false)));
        }
        return name;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int index = 0; index < originalLoreKeys.size(); index++) {
            boolean heading = index == 0;
            int color = heading ? 0xFFFFFF : 0xAAAAAA;
            if (heading) {
                MutableComponent line = Component.translatable("item.carrot_on_a_stick.5.lore.1.1")
                        .withStyle(style -> style.withColor(color).withBold(true).withItalic(false))
                        .append(Component.translatable(originalLoreKeys.get(index))
                                .withStyle(style -> style.withColor(color).withBold(false).withItalic(false)));
                tooltip.add(line);
            } else {
                tooltip.add(Component.translatable(originalLoreKeys.get(index))
                        .withStyle(style -> style.withColor(color).withItalic(false)));
            }
        }
        tooltip.add(Component.translatable("item.carrot_on_a_stick.5.lore.6.1")
                .withStyle(style -> style.withColor(0xAAAAAA).withItalic(false)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            prepare(stack, isCharged(player));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().getItem() instanceof WandItem) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !isCharged(serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        prepare(stack, false);
        execute(serverPlayer, new ResourceLocation("skills", "spells/spell_" + spellId + "_cast"));
        execute(serverPlayer, CAST_AFTER);
        return InteractionResultHolder.success(stack);
    }

    private void prepare(ItemStack stack, boolean charged) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("is_spell", true);
        tag.putByte("spell", (byte) spellId);
        tag.putBoolean(CHARGED, charged);
        abilityTags.forEach(abilityTag -> tag.putBoolean(abilityTag, true));
        for (EnchantmentEntry entry : enchantments) {
            Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(entry.id());
            if (enchantment != null && EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) < entry.level()) {
                stack.enchant(enchantment, entry.level());
            }
        }
    }

    private static boolean isCharged(ServerPlayer player) {
        Objective objective = player.getScoreboard().getObjective("spell_cd");
        if (objective == null) {
            return false;
        }
        return player.getScoreboard().getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore() >= 20;
    }

    private static void execute(ServerPlayer player, ResourceLocation id) {
        player.server.getFunctions().get(id).ifPresent(function -> player.server.getFunctions().execute(
                function, player.createCommandSourceStack().withPermission(2).withSuppressedOutput()));
    }

    public record Modifier(ResourceLocation attribute, double amount, AttributeModifier.Operation operation) {
    }

    public record EnchantmentEntry(ResourceLocation id, int level) {
    }
}
