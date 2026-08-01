package io.github.finalparadox.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-dimension orchestrator for the B5 dual fight. The encounter state lives
 * in {@link B5EncounterData}; the runtime controller is ticked from the server
 * tick event so the fight survives boss deaths and reloads.
 */
public final class B5EncounterManager {
    private static final Map<String, B5EncounterController> CONTROLLERS = new ConcurrentHashMap<>();

    private B5EncounterManager() {
    }

    private static String key(ServerLevel level) {
        return level.dimension().location().toString();
    }

    public static void beginCountdown(ServerLevel level, BlockPos anchor,
                                      KoyomiBossEntity koyo, GariBossEntity gari) {
        B5EncounterData data = B5EncounterData.get(level);
        B5EncounterController controller = new B5EncounterController();
        CONTROLLERS.put(key(level), controller);
        data.setAnchor(anchor);
        controller.prepare(level, data, koyo, gari);
    }

    public static void tick(ServerLevel level) {
        tickRewardParticles(level);
        B5EncounterData data = B5EncounterData.get(level);
        if (!data.active()) {
            CONTROLLERS.remove(key(level));
            return;
        }
        B5EncounterController controller = CONTROLLERS.get(key(level));
        if (controller == null) {
            controller = new B5EncounterController();
            CONTROLLERS.put(key(level), controller);
            controller.recover(level, data);
        }
        controller.tick(level, data);
    }

    private static void tickRewardParticles(ServerLevel level) {
        boolean groundedParticle = level.random.nextBoolean();
        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (!(entity instanceof ItemEntity item) || !item.isAlive()) continue;
            if (!item.getItem().hasTag() || !item.getItem().getTag().getBoolean("chapa_gariheuz")) continue;
            if (!item.onGround() || groundedParticle) {
                level.sendParticles(ParticleTypes.FIREWORK,
                        item.getX(), item.getY() + 0.4D, item.getZ(), 1, 0, 0, 0,
                        item.onGround() ? 0.2D : 0.0D);
            }
        }
    }

    public static void onBossHit(ServerLevel level, boolean koyoHit, Vec3 pos) {
        B5EncounterController controller = CONTROLLERS.get(key(level));
        if (controller != null) controller.onBossHit(level, koyoHit, pos);
    }

    public static boolean isActive(ServerLevel level) {
        return B5EncounterData.get(level).active();
    }

    public static void onPlayerDeath(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        B5EncounterData data = B5EncounterData.get(level);
        if (data.active()) data.markDeadPlayer(player.getUUID());
    }

    public static void reset(ServerLevel level) {
        B5EncounterData data = B5EncounterData.get(level);
        B5EncounterController controller = CONTROLLERS.remove(key(level));
        if (controller != null && data.active()) {
            controller.endEncounterPublic(level, data);
        } else if (data.active()) {
            controller = new B5EncounterController();
            controller.recover(level, data);
            controller.endEncounterPublic(level, data);
        }
    }

    public static UUID koyoUuid(ServerLevel level) {
        return B5EncounterData.get(level).koyoUuid();
    }

    public static UUID gariUuid(ServerLevel level) {
        return B5EncounterData.get(level).gariUuid();
    }
}
