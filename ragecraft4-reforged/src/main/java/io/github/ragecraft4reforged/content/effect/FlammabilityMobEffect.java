package io.github.ragecraft4reforged.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class FlammabilityMobEffect extends MobEffect {
    public FlammabilityMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x339900);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.isOnFire() && !entity.fireImmune()
                && !entity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            EffectDamage.hurtIgnoringInvulnerabilityFrames(
                    entity, entity.damageSources().onFire(), amplifier + 1.0f);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
