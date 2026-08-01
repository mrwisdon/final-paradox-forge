package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.ConquerorShadowBossEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Keeps command-source boss minions faithful to their empty death loot tables. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BossMinionDropEvents {
    private BossMinionDropEvents() {
    }

    @SubscribeEvent
    public static void clearStyxMemoryDrops(LivingDropsEvent event) {
        if (ConquerorShadowBossEntity.isStyxMemory(event.getEntity())) event.getDrops().clear();
    }

    @SubscribeEvent
    public static void clearStyxMemoryExperience(LivingExperienceDropEvent event) {
        if (ConquerorShadowBossEntity.isStyxMemory(event.getEntity())) event.setDroppedExperience(0);
    }
}
