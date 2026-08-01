package io.github.ragecraft4reforged.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A native, single-stack form of one original infinite lingering or drinkable potion. */
public final class InfinitePotionItem extends Item {
    public static final String INFINITE_POTION = "infinite_potion";
    public static final String POTION_TYPE = "potion_type";

    private final InfinitePotionDefinition definition;

    public InfinitePotionItem(InfinitePotionDefinition definition) {
        super(new Item.Properties().stacksTo(1));
        this.definition = definition;
    }

    public InfinitePotionDefinition definition() {
        return definition;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        prepare(stack);
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        return InfinitePotionText.name(definition);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        InfinitePotionText.appendLore(definition, tooltip);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            prepare(stack);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (definition.drinkable()) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LINGERING_POTION_THROW,
                SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        ThrownPotion projectile = new ThrownPotion(level, player);
        projectile.setItem(getDefaultInstance());
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
        InfinitePotionRuntime.markThrown(projectile, serverPlayer, definition);
        level.addFreshEntity(projectile);
        player.awardStat(Stats.ITEM_USED.get(this));
        InfinitePotionRuntime.beginCooldown(serverPlayer, definition);
        return InteractionResultHolder.success(InfinitePotionRuntime.emptyStack(definition));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            player.awardStat(Stats.ITEM_USED.get(this));
            InfinitePotionRuntime.activateDrink(player, definition);
            InfinitePotionRuntime.beginCooldown(player, definition);
            return InfinitePotionRuntime.emptyStack(definition);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return definition.drinkable() ? 32 : 0;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return definition.drinkable() ? UseAnim.DRINK : UseAnim.NONE;
    }

    private void prepare(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(INFINITE_POTION, true);
        tag.putInt(POTION_TYPE, definition.potionType());
    }
}
