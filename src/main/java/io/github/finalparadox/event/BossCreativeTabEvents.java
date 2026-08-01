package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BossCreativeTabEvents {
    private BossCreativeTabEvents() {
    }

    @SubscribeEvent
    public static void addSpawnEgg(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.APIGLO_SPAWN_EGG.get());
            event.accept(ModItems.THAR_KROO_SPAWN_EGG.get());
            event.accept(ModItems.CONQUEROR_SHADOW_SPAWN_EGG.get());
            event.accept(ModItems.KOYOMI_SPAWN_EGG.get());
            event.accept(ModItems.GARI_SPAWN_EGG.get());
        }
    }
}
