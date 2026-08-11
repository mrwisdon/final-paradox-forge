package io.github.finalparadox;

import io.github.finalparadox.network.ModNetwork;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModCreativeTabs;
import io.github.finalparadox.registry.ModBlocks;
import io.github.finalparadox.registry.ModItems;
import io.github.finalparadox.registry.ModSounds;
import io.github.finalparadox.worldgen.ModWorldgen;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FinalParadox.MOD_ID)
public final class FinalParadox {
    public static final String MOD_ID = "finalparadox";

    public FinalParadox(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModBlocks.REGISTER.register(modEventBus);
        ModItems.REGISTER.register(modEventBus);
        ModCreativeTabs.REGISTER.register(modEventBus);
        ModEntities.REGISTER.register(modEventBus);
        ModSounds.REGISTER.register(modEventBus);
        ModWorldgen.register(modEventBus);
        ModNetwork.register();
    }
}
