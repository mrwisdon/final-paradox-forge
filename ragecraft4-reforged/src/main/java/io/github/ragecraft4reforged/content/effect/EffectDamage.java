package io.github.ragecraft4reforged.content.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class EffectDamage {
    public static void hurtIgnoringInvulnerabilityFrames(LivingEntity entity, DamageSource source, float amount) {
        if (amount <= 0.0f || entity.getTags().contains("invulnerable") || entity.isInvulnerableTo(source)) {
            return;
        }
        int previousInvulnerableTime = entity.invulnerableTime;
        entity.invulnerableTime = 0;
        try {
            entity.hurt(source, amount);
        } finally {
            if (entity.isAlive()) {
                entity.invulnerableTime = previousInvulnerableTime;
            }
        }
    }

    private EffectDamage() {
    }
}
