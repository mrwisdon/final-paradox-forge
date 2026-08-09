package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FinalParadox.MOD_ID);

    public static final RegistryObject<SoundEvent> APIGLO_INTRO = register("apiglo_intro");
    public static final RegistryObject<SoundEvent> APIGLO_LOOP = register("apiglo_loop");
    public static final RegistryObject<SoundEvent> APIGLO_ENTRANCE_INTRO = register("apiglo_entrance_intro");
    public static final RegistryObject<SoundEvent> APIGLO_ENTRANCE_LOOP = register("apiglo_entrance_loop");
    public static final RegistryObject<SoundEvent> THAR_KROO = register("thar_kroo");
    public static final RegistryObject<SoundEvent> CONQUEROR_SHADOW_INTRO = register("conqueror_shadow_intro");
    public static final RegistryObject<SoundEvent> CONQUEROR_SHADOW_LOOP = register("conqueror_shadow_loop");
    public static final RegistryObject<SoundEvent> MARAWTHAR_INTRO = register("marawthar_intro");
    public static final RegistryObject<SoundEvent> MARAWTHAR_INTERMISSION = register("marawthar_intermission");
    public static final RegistryObject<SoundEvent> MARAWTHAR_LOOP = register("marawthar_loop");
    public static final RegistryObject<SoundEvent> MARAWTHAR_VICTORY = register("marawthar_victory");
    public static final RegistryObject<SoundEvent> KOYOMI_MAIN_INTRO = register("koyomi_main_intro");
    public static final RegistryObject<SoundEvent> KOYOMI_MAIN_LOOP = register("koyomi_main_loop");
    public static final RegistryObject<SoundEvent> KOYOMI_INTER_INTRO = register("koyomi_inter_intro");
    public static final RegistryObject<SoundEvent> KOYOMI_INTER_LOOP = register("koyomi_inter_loop");
    public static final RegistryObject<SoundEvent> KOYOMI_INTER_FINAL = register("koyomi_inter_final");
    public static final RegistryObject<SoundEvent> ABATIR_JEFE = register("abatir_jefe");
    public static final RegistryObject<SoundEvent> B8_ABORDO_LOOP = register("b8_abordo_loop");
    public static final RegistryObject<SoundEvent> DRONE_GATLING_SPINUP =
            register("drone_gatling_spinup");
    public static final RegistryObject<SoundEvent> DRONE_GATLING_FIRE =
            register("drone_gatling_fire");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, name);
        return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    private ModSounds() {
    }
}
