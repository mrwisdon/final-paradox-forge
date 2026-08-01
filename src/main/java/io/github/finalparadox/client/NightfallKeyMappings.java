package io.github.finalparadox.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.ability.NightfallAbilityState;
import io.github.finalparadox.network.ModNetwork;
import io.github.finalparadox.network.NightfallAbilityPacket;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class NightfallKeyMappings {
    private static final String CATEGORY = "key.categories.finalparadox";

    public static final KeyMapping COMBO = key("key.finalparadox.nightfall.combo", GLFW.GLFW_KEY_Z);
    public static final KeyMapping RIFT = key("key.finalparadox.nightfall.rift", GLFW.GLFW_KEY_X);
    public static final KeyMapping LASER = key("key.finalparadox.nightfall.laser", GLFW.GLFW_KEY_C);
    public static final KeyMapping SMALL_LASER =
            key("key.finalparadox.nightfall.small_laser", GLFW.GLFW_KEY_V);
    public static final KeyMapping CHAIN_BLADE =
            key("key.finalparadox.nightfall.chain_blade", GLFW.GLFW_KEY_B);

    private NightfallKeyMappings() {
    }

    private static KeyMapping key(String translationKey, int defaultKey) {
        return new KeyMapping(translationKey, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        boolean canActivate = minecraft.screen == null
                && isHoldingNightfall(minecraft)
                && !NightfallNormalAttackAnimationState.isActive(minecraft.player);

        consumeClicks(COMBO, NightfallAbilityState.AbilityMode.COMBO, canActivate);
        consumeClicks(RIFT, NightfallAbilityState.AbilityMode.RIFT, canActivate);
        consumeClicks(LASER, NightfallAbilityState.AbilityMode.LASER, canActivate);
        consumeClicks(SMALL_LASER, NightfallAbilityState.AbilityMode.SMALL_LASER, canActivate);
        consumeClicks(CHAIN_BLADE, NightfallAbilityState.AbilityMode.CHAIN_BLADE, canActivate);
    }

    private static boolean isHoldingNightfall(Minecraft minecraft) {
        return minecraft.player.getMainHandItem().is(ModItems.NIGHTFALL.get())
                || minecraft.player.getOffhandItem().is(ModItems.NIGHTFALL.get());
    }

    private static void consumeClicks(KeyMapping key, NightfallAbilityState.AbilityMode mode,
                                      boolean canActivate) {
        while (key.consumeClick()) {
            if (canActivate) {
                ModNetwork.CHANNEL.sendToServer(new NightfallAbilityPacket(mode.ordinal()));
            }
        }
    }
}
