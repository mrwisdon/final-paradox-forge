package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;

/**
 * Keeps entities already stored as finalparadox:marota loadable after the
 * public registry id was renamed to finalparadox:marawthar.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LegacyRegistryMappings {
    private LegacyRegistryMappings() {
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<EntityType<?>> mapping
                : event.getMappings(Registries.ENTITY_TYPE, FinalParadox.MOD_ID)) {
            if (mapping.getKey().getPath().equals("marota")) {
                mapping.remap(ModEntities.MARAWTHAR.get());
            }
        }
    }
}
