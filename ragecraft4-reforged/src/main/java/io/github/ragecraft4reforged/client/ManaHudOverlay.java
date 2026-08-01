package io.github.ragecraft4reforged.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.ragecraft4reforged.Ragecraft4Reforged;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ragecraft4Reforged.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ManaHudOverlay {
    private static final int ICON_COUNT = 10;
    private static final int ICON_SIZE = 9;
    private static final int ICON_STEP = 8;
    private static final int BAR_WIDTH = ICON_SIZE + (ICON_COUNT - 1) * ICON_STEP;
    private static final int TEXTURE_HEIGHT = 27;
    private static final int ICON_V = 18;
    private static final ResourceLocation EMPTY = texture("mana_empty");
    private static final ResourceLocation HALF = texture("mana_half");
    private static final ResourceLocation FULL = texture("mana_full");
    private static final ResourceLocation GLOWING = texture("mana_glowing");

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientManaData.isSynchronized() || minecraft.player == null || minecraft.options.hideGui
                || minecraft.player.isCreative() || minecraft.player.isSpectator()) {
            return;
        }

        int right = event.getWindow().getGuiScaledWidth() / 2 + 91;
        int x = right - BAR_WIDTH;
        boolean airBarVisible = minecraft.player.getAirSupply() < minecraft.player.getMaxAirSupply();
        int y = event.getWindow().getGuiScaledHeight() - (airBarVisible ? 59 : 49);
        int mana = Mth.clamp(ClientManaData.mana(), 0, 20);
        GuiGraphics graphics = event.getGuiGraphics();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int index = 0; index < ICON_COUNT; index++) {
            ResourceLocation texture;
            if (mana >= 20) {
                texture = GLOWING;
            } else {
                int cellValue = Mth.clamp(mana - (18 - index * 2), 0, 2);
                texture = cellValue == 2 ? FULL : cellValue == 1 ? HALF : EMPTY;
            }
            graphics.blit(texture, x + index * ICON_STEP, y, 0, ICON_V,
                    ICON_SIZE, ICON_SIZE, ICON_SIZE, TEXTURE_HEIGHT);
        }
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientManaData.reset();
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation("rc4", "textures/font/manabar/" + name + ".png");
    }

    private ManaHudOverlay() {
    }
}
