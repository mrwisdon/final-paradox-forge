package io.github.ragecraft4reforged.registry;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.content.effect.DecayMobEffect;
import io.github.ragecraft4reforged.content.effect.FlammabilityMobEffect;
import io.github.ragecraft4reforged.content.effect.MarkerMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> REGISTER =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Ragecraft4Reforged.MOD_ID);

    public static final RegistryObject<MobEffect> DECAY = REGISTER.register("decay", DecayMobEffect::new);
    public static final RegistryObject<MobEffect> FLAMMABILITY = REGISTER.register(
            "flammability", FlammabilityMobEffect::new);
    public static final RegistryObject<MobEffect> VULNERABILITY = REGISTER.register("vulnerability", () ->
            new MarkerMobEffect(MobEffectCategory.HARMFUL, 0x484D48));

    private ModMobEffects() {
    }
}
