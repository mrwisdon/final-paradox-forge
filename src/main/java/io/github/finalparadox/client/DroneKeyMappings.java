package io.github.finalparadox.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class DroneKeyMappings {
    public static final KeyMapping DROP_BOMB = new KeyMapping(
            "key.finalparadox.recon_drone.drop_bomb",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.finalparadox");
    public static final KeyMapping EXIT = new KeyMapping(
            "key.finalparadox.recon_drone.exit",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.finalparadox");

    private DroneKeyMappings() {
    }
}
