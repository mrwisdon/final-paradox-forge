package io.github.finalparadox.item;

import io.github.finalparadox.ability.NightfallAbilityState;
import io.github.finalparadox.client.NightfallItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

/**
 * A survival-usable reconstruction of the blade carried by MarawThar in B9.
 *
 * <p>The map's reward item is deliberately powerless after the final battle.
 * This mod item restores five characteristic boss attacks instead: the
 * three-hit pursuit combo, the charged blue travelling slash, the
 * two B9 laser forms, and the chainblade skyfall.</p>
 */
public final class NightfallItem extends SwordItem {
    public NightfallItem() {
        super(Tiers.NETHERITE, 5, -2.4F,
                new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // The original B9 loadout reserves right-click for Atacrom's off-hand
        // gauntlet. Nightfall's techniques already have dedicated keybinds, so
        // let Minecraft continue to the off-hand instead of consuming the click.
        if (hand == InteractionHand.MAIN_HAND
                && player.getOffhandItem().is(io.github.finalparadox.registry.ModItems.ATACROM_GAUNTLET.get())) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NightfallAbilityState.tryStart(
                    serverPlayer, NightfallAbilityState.AbilityMode.COMBO, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepare(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        prepare(stack);
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    private static void prepare(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Unbreakable", true);
        stack.getOrCreateTag().putInt("HideFlags", 4);
        if (stack.getEnchantmentLevel(Enchantments.SHARPNESS) < 5) {
            stack.enchant(Enchantments.SHARPNESS, 5);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private NightfallItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    renderer = new NightfallItemRenderer(
                            minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.empty());
        for (int line = 1; line <= 23; line++) {
            String loreKey = "item.finalparadox.nightfall.lore." + line;
            tooltip.add(line >= 13 && line <= 17
                    ? Component.translatable(loreKey, Component.keybind(abilityKeyForLoreLine(line)))
                    : Component.translatable(loreKey));
            if (line == 2 || line == 11 || line == 17 || line == 22) {
                tooltip.add(Component.empty());
            }
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.finalparadox.legendary"));
    }

    private static String abilityKeyForLoreLine(int line) {
        return switch (line) {
            case 13 -> "key.finalparadox.nightfall.combo";
            case 14 -> "key.finalparadox.nightfall.rift";
            case 15 -> "key.finalparadox.nightfall.laser";
            case 16 -> "key.finalparadox.nightfall.small_laser";
            case 17 -> "key.finalparadox.nightfall.chain_blade";
            default -> throw new IllegalArgumentException("Not a Nightfall control line: " + line);
        };
    }
}
