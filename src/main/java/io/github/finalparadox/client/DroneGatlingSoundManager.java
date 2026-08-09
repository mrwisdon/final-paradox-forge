package io.github.finalparadox.client;

import io.github.finalparadox.entity.DroneEntity;
import io.github.finalparadox.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Starts and stops attached Gatling sounds for every drone tracked by this client. */
final class DroneGatlingSoundManager {
    private static final Map<Integer, TrackedSound> TRACKED = new HashMap<>();

    private DroneGatlingSoundManager() {
    }

    static void tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            stopAll();
            return;
        }

        Set<Integer> seen = new HashSet<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof DroneEntity drone)) {
                continue;
            }
            seen.add(drone.getId());
            updateDrone(minecraft, drone);
        }

        Iterator<Map.Entry<Integer, TrackedSound>> iterator = TRACKED.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, TrackedSound> entry = iterator.next();
            if (!seen.contains(entry.getKey())) {
                entry.getValue().sound.requestStop();
                iterator.remove();
            }
        }
    }

    private static void updateDrone(Minecraft minecraft, DroneEntity drone) {
        int state = drone.getGatlingState();
        TrackedSound tracked = TRACKED.get(drone.getId());
        if (tracked != null && tracked.drone == drone && tracked.state == state) {
            return;
        }
        if (tracked != null) {
            tracked.sound.requestStop();
            TRACKED.remove(drone.getId());
        }
        if (state == DroneEntity.GATLING_IDLE) {
            return;
        }

        DroneGatlingSoundInstance sound;
        if (state == DroneEntity.GATLING_WARMING) {
            sound = new DroneGatlingSoundInstance(
                    drone, state, ModSounds.DRONE_GATLING_SPINUP.get(), false);
        } else if (state == DroneEntity.GATLING_FIRING) {
            sound = new DroneGatlingSoundInstance(
                    drone, state, ModSounds.DRONE_GATLING_FIRE.get(), true);
        } else {
            return;
        }
        TRACKED.put(drone.getId(), new TrackedSound(drone, state, sound));
        minecraft.getSoundManager().play(sound);
    }

    private static void stopAll() {
        for (TrackedSound tracked : TRACKED.values()) {
            tracked.sound.requestStop();
        }
        TRACKED.clear();
    }

    private record TrackedSound(
            DroneEntity drone, int state, DroneGatlingSoundInstance sound) {
    }
}
