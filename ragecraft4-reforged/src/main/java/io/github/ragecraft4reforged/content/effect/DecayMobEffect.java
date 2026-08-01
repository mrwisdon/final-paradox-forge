package io.github.ragecraft4reforged.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class DecayMobEffect extends MobEffect {
    public DecayMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x1DC2D1);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance wither = entity.getEffect(MobEffects.WITHER);
        if (!entity.level().isClientSide && wither != null) {
            float damage = 0.5f * (amplifier + 1) * (wither.getAmplifier() + 1);
            EffectDamage.hurtIgnoringInvulnerabilityFrames(entity, entity.damageSources().wither(), damage);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
