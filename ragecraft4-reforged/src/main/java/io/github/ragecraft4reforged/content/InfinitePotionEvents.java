package io.github.ragecraft4reforged.content;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ragecraft4Reforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class InfinitePotionEvents {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            InfinitePotionRuntime.refillIfReady(player);
        }
    }

    @SubscribeEvent
    public static void onPotionImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownPotion projectile) || projectile.level().isClientSide) {
            return;
        }
        ItemStack stack = projectile.getItem();
        if (!(stack.getItem() instanceof InfinitePotionItem potionItem) || potionItem.definition().drinkable()) {
            return;
        }

        projectile.setPos(event.getRayTraceResult().getLocation());
        InfinitePotionRuntime.activateThrown(projectile, potionItem.definition());
        event.setCanceled(true);
        projectile.discard();
    }

    private InfinitePotionEvents() {
    }
}
