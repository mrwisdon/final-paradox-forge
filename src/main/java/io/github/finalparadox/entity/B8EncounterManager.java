package io.github.finalparadox.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-dimension orchestrator for the B8 Zombie Supermatrix encounter. State
 * lives in {@link B8EncounterData}; the runtime controller is ticked from the
 * server tick event so the fight survives reloads.
 */
public final class B8EncounterManager {
    private static final Map<String, B8EncounterController> CONTROLLERS = new ConcurrentHashMap<>();

    private B8EncounterManager() {
    }

    private static String key(ServerLevel level) {
        return level.dimension().location().toString();
    }

    public static boolean begin(ServerLevel level, BlockPos anchor) {
        B8EncounterData data = B8EncounterData.get(level);
        if (data.active()) return false;
        B8EncounterController controller = new B8EncounterController();
        CONTROLLERS.put(key(level), controller);
        data.setAnchor(anchor);
        controller.prepare(level, data);
        return true;
    }

    public static void tick(ServerLevel level) {
        B8EncounterController controller = requireController(level);
        if (controller == null) return;
        controller.tick(level, B8EncounterData.get(level));
    }

    public static void reset(ServerLevel level) {
        B8EncounterData data = B8EncounterData.get(level);
        B8EncounterController controller = CONTROLLERS.remove(key(level));
        if (controller != null && data.active()) {
            controller.endEncounter(level, data);
        } else if (data.active()) {
            controller = new B8EncounterController();
            controller.recover(level, data);
            controller.endEncounter(level, data);
        }
    }

    public static boolean isActive(ServerLevel level) {
        return B8EncounterData.get(level).active();
    }

    public static Optional<B8EncounterController> controller(ServerLevel level) {
        return Optional.ofNullable(CONTROLLERS.get(key(level)));
    }

    public static B8EncounterData encounterData(ServerLevel level) {
        return B8EncounterData.get(level);
    }

    /**
     * Returns the active controller, creating and recovering it from saved
     * data when needed. Returns null when no encounter is active.
     */
    public static B8EncounterController requireController(ServerLevel level) {
        B8EncounterData data = B8EncounterData.get(level);
        if (!data.active()) {
            CONTROLLERS.remove(key(level));
            return null;
        }
        B8EncounterController controller = CONTROLLERS.get(key(level));
        if (controller == null) {
            controller = new B8EncounterController();
            CONTROLLERS.put(key(level), controller);
            controller.recover(level, data);
        }
        return controller;
    }

    public static void onPlayerDeath(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (isActive(level)) {
            B8EncounterData.get(level).addSpectator(player.getUUID());
        }
    }

    /** Rover bullet hook: returns MISS/BLOCKED/HIT against the active matrix. */
    public static int testBullet(ServerLevel level, Vec3 bulletPosition) {
        B8EncounterController controller = requireController(level);
        return controller != null
                ? controller.testBullet(bulletPosition)
                : B8EncounterController.BULLET_MISS;
    }

    public static Vec3 bulletHitCenter(ServerLevel level) {
        B8EncounterController controller = requireController(level);
        return controller != null ? controller.bulletHitCenter() : Vec3.ZERO;
    }

    /** Rover bullet hook: returns true when a module was broken by the bullet. */
    public static boolean testModuleHit(ServerLevel level, Vec3 bulletPosition) {
        B8EncounterController controller = requireController(level);
        return controller != null && controller.testModuleHit(level, bulletPosition);
    }

    public static void onModuleLanded(ServerLevel level, B8H2ModuleEntity module) {
        B8EncounterController controller = requireController(level);
        if (controller != null) controller.onModuleLanded(level, module);
    }

    /** Sniper bullet tick: returns true when the bullet exploded and was discarded. */
    public static boolean onSniperBulletTick(ServerLevel level, B8SniperBulletEntity bullet) {
        B8EncounterController controller = requireController(level);
        return controller != null && controller.onSniperBulletTick(level, bullet);
    }

    /** h3/zombie_robot/hit + h3/tnt/hit burst on a damaged add. */
    public static void burstAdd(Mob add) {
        if (!(add.level() instanceof ServerLevel server)) return;
        add.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        add.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        add.setHealth(Math.min(add.getMaxHealth(), 20.0F));
        add.addTag("b8_h3_reventado");
        server.sendParticles(ParticleTypes.LAVA,
                add.getX(), add.getY() + 1.0D, add.getZ(), 3, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                add.getX(), add.getY() + 1.2D, add.getZ(), 20, 0.0D, 0.0D, 0.0D, 0.5D);
        server.sendParticles(new ItemParticleOption(
                        ParticleTypes.ITEM, new ItemStack(Items.GRAY_TERRACOTTA)),
                add.getX(), add.getY() + 1.2D, add.getZ(), 100, 0.0D, 0.0D, 0.0D, 0.3D);
        server.playSound(null, add.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.MASTER, 1.0F, 2.0F);
    }
}
