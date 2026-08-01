package io.github.finalparadox.item;

import io.github.finalparadox.entity.GreatHookEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public final class GreatHookItem extends Item {
    public static final String COOLDOWN_KEY="finalparadox.great_hook_cooldown";
    public static final int COOLDOWN_TICKS=34;
    public GreatHookItem(){super(new Properties().stacksTo(1));}
    @Override public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand){ItemStack stack=player.getItemInHand(hand);if(player instanceof ServerPlayer server){if(!GreatHookEntity.toggleOrSpawn(server)&&server.getPersistentData().getInt(COOLDOWN_KEY)>0)server.displayClientMessage(Component.translatable("message.finalparadox.great_hook.cooldown"),true);}return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
    @Override public ItemStack getDefaultInstance(){ItemStack stack=super.getDefaultInstance();prepare(stack);return stack;}
    @Override public void inventoryTick(ItemStack stack,Level level,Entity entity,int slot,boolean selected){prepare(stack);}
    private static void prepare(ItemStack stack){stack.getOrCreateTag().putBoolean("Unbreakable",true);stack.getOrCreateTag().putInt("HideFlags",4);}
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag){tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.great_hook.use"));tooltip.add(Component.empty());for(int i=1;i<=5;i++)tooltip.add(Component.translatable("item.finalparadox.great_hook.lore."+i));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.great_hook.length"));tooltip.add(Component.translatable("item.finalparadox.great_hook.cooldown"));tooltip.add(Component.empty());tooltip.add(Component.translatable("item.finalparadox.mythic"));}
}
