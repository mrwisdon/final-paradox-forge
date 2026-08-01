package io.github.ragecraft4reforged.runeforge;

import io.github.ragecraft4reforged.Ragecraft4Reforged;
import io.github.ragecraft4reforged.registry.ModEntityTags;
import io.github.ragecraft4reforged.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Ragecraft4Reforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SigilDropEvents {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)
                || !(event.getSource().getEntity() instanceof Player)
                || !(target instanceof Enemy || target.getType().is(ModEntityTags.SIGIL_SOURCES))) {
            return;
        }

        float chance = dropChance(target);
        chance = Math.min(1.0F, chance + event.getLootingLevel() * 0.015F);
        int prefixWeight = PrefixTool.values().length;
        int suffixWeight = SuffixSchool.values().length;
        chance *= (float) suffixWeight / (prefixWeight + suffixWeight);
        if (target.getRandom().nextFloat() >= chance) {
            return;
        }

        ItemStack stack = ModItems.suffixSigil(schoolFor(target)).getDefaultInstance();
        event.getDrops().add(new ItemEntity(level, target.getX(), target.getY(), target.getZ(), stack));
    }

    private static float dropChance(LivingEntity target) {
        if (target instanceof EnderDragon || target instanceof WitherBoss || target.getMaxHealth() >= 200.0F) {
            return 1.0F;
        }
        if (target.getMaxHealth() >= 60.0F) {
            return 0.10F;
        }
        if (target.level().dimension().equals(Level.END)) {
            return 0.03F;
        }
        if (target.level().dimension().equals(Level.NETHER)) {
            return 0.02F;
        }
        return 0.01F;
    }

    private static SuffixSchool schoolFor(LivingEntity target) {
        List<SuffixSchool> matches = new ArrayList<>();
        for (SuffixSchool school : SuffixSchool.values()) {
            if (target.getType().is(school.sourceTag())) {
                matches.add(school);
            }
        }
        boolean useAffinity = !matches.isEmpty() && target.getRandom().nextFloat() < 0.8F;
        SuffixSchool[] pool = useAffinity ? matches.toArray(SuffixSchool[]::new) : SuffixSchool.values();
        return pool[target.getRandom().nextInt(pool.length)];
    }

    private SigilDropEvents() {
    }
}
