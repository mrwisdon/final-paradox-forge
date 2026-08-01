package io.github.finalparadox.arena;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.B5EncounterManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArenaDeploymentEvents {
    private ArenaDeploymentEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            ArenaDeploymentManager.tick(level);
            B5EncounterManager.tick(level);
        }
    }
}
