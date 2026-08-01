package io.github.finalparadox.item;

import io.github.finalparadox.entity.RuinHammerSwingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public final class RuinCreatorItem extends Item {
    public static final String CHARGE_KEY="finalparadox.ruin_creator_charge";
    public RuinCreatorItem(){super(new Properties().stacksTo(1));}
    @Override public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player,InteractionHand hand){ItemStack stack=player.getItemInHand(hand);player.startUsingItem(hand);if(player instanceof ServerPlayer server)RuinHammerSwingEntity.activate(server);return InteractionResultHolder.consume(stack);}
    @Override public void onUseTick(Level level,LivingEntity user,ItemStack stack,int remaining){if(user instanceof ServerPlayer player)RuinHammerSwingEntity.activate(player);}
    @Override public int getUseDuration(ItemStack stack){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.NONE;}
    public static void recordHit(ServerPlayer player){int charge=Math.min(12,player.getPersistentData().getInt(CHARGE_KEY)+1);player.getPersistentData().putInt(CHARGE_KEY,charge);player.displayClientMessage(Component.translatable("message.finalparadox.ruin_creator.charge",charge),true);}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.ruin_creator.attack"));tooltip.add(Component.empty());for(int i=1;i<=8;i++)tooltip.add(Component.translatable("item.finalparadox.ruin_creator.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.ruin_creator.damage"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.rare"));tooltip.add(Component.empty());}
}
