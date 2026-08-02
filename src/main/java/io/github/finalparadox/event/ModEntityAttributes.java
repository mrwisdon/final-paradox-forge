package io.github.finalparadox.event;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.world.entity.animal.Bee;
import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.TharKrooBossEntity;
import io.github.finalparadox.entity.ConquerorShadowBossEntity;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.KoyomiBossEntity;
import io.github.finalparadox.entity.GariBossEntity;
import io.github.finalparadox.entity.B8DetonatorBombEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=FinalParadox.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityAttributes {
    private ModEntityAttributes(){}
    @SubscribeEvent public static void create(EntityAttributeCreationEvent event){
        event.put(ModEntities.SUMMONED_BEE.get(),Bee.createAttributes().build());
        event.put(ModEntities.APIGLO.get(), ApigloBossEntity.createAttributes().build());
        event.put(ModEntities.THAR_KROO.get(), TharKrooBossEntity.createAttributes().build());
        event.put(ModEntities.CONQUEROR_SHADOW.get(), ConquerorShadowBossEntity.createAttributes().build());
        event.put(ModEntities.MARAWTHAR.get(), MarawTharBossEntity.createAttributes().build());
        event.put(ModEntities.KOYOMI.get(), KoyomiBossEntity.createAttributes().build());
        event.put(ModEntities.GARI.get(), GariBossEntity.createAttributes().build());
        event.put(ModEntities.B8_DETONATOR_BOMB.get(), B8DetonatorBombEntity.createAttributes().build());
    }
}
