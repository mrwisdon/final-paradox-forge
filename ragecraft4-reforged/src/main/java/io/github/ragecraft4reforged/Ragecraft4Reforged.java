package io.github.ragecraft4reforged;

import io.github.ragecraft4reforged.registry.ModBlocks;
import io.github.ragecraft4reforged.registry.ModBlockEntities;
import io.github.ragecraft4reforged.registry.ModCreativeTabs;
import io.github.ragecraft4reforged.registry.ModEnchantments;
import io.github.ragecraft4reforged.registry.ModItems;
import io.github.ragecraft4reforged.registry.ModMenus;
import io.github.ragecraft4reforged.registry.ModMobEffects;
import io.github.ragecraft4reforged.registry.ModRecipeSerializers;
import io.github.ragecraft4reforged.network.ModNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Ragecraft4Reforged.MOD_ID)
public final class Ragecraft4Reforged {
    public static final String MOD_ID = "ragecraft4reforged";

    public Ragecraft4Reforged(FMLJavaModLoadingContext context) {
        ModNetwork.init();
        IEventBus modEventBus = context.getModEventBus();
        ModBlocks.REGISTER.register(modEventBus);
        ModBlockEntities.REGISTER.register(modEventBus);
        ModItems.REGISTER.register(modEventBus);
        ModMenus.REGISTER.register(modEventBus);
        ModEnchantments.REGISTER.register(modEventBus);
        ModMobEffects.REGISTER.register(modEventBus);
        ModRecipeSerializers.REGISTER.register(modEventBus);
        ModCreativeTabs.REGISTER.register(modEventBus);
    }
}
