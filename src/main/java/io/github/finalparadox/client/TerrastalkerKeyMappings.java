package io.github.finalparadox.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class TerrastalkerKeyMappings {
    public static final KeyMapping DISMOUNT = new KeyMapping(
            "key.finalparadox.terrastalker.dismount",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.finalparadox");

    private TerrastalkerKeyMappings() {
    }
}
