package io.github.finalparadox.client;

import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** A positional Gatling sound that follows one tracked drone and stops on state change. */
final class DroneGatlingSoundInstance extends AbstractTickableSoundInstance {
    private final DroneEntity drone;
    private final int requiredState;

    DroneGatlingSoundInstance(
            DroneEntity drone, int requiredState, SoundEvent sound, boolean looping) {
        super(sound, SoundSource.PLAYERS, RandomSource.create());
        this.drone = drone;
        this.requiredState = requiredState;
        this.looping = looping;
        this.delay = 0;
        this.volume = 1.15F;
        this.pitch = 1.0F;
        this.attenuation = Attenuation.LINEAR;
        updatePosition();
    }

    @Override
    public void tick() {
        if (drone.isRemoved() || drone.getGatlingState() != requiredState) {
            stop();
            return;
        }
        updatePosition();
    }

    void requestStop() {
        stop();
    }

    private void updatePosition() {
        x = drone.getX();
        y = drone.getY() - 1.1D;
        z = drone.getZ();
    }
}
