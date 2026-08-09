package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.List;

/** Mod-bus registration required by the recon-drone body proxy. */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DroneModEvents {
    private DroneModEvents() {
    }

    @SubscribeEvent
    public static void onCreateAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.RECON_DRONE_BODY.get(),
                LivingEntity.createLivingAttributes().build());
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> ForgeChunkManager.setForcedChunkLoadingCallback(
                FinalParadox.MOD_ID,
                (level, tickets) -> List.copyOf(tickets.getEntityTickets().keySet())
                        .forEach(tickets::removeAllTickets)));
    }
}
