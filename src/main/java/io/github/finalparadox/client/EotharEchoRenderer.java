package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.finalparadox.entity.EotharEchoEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

public final class EotharEchoRenderer extends EntityRenderer<EotharEchoEntity> {
    private final Font font;

    public EotharEchoRenderer(EntityRendererProvider.Context context) {
        super(context);
        font = context.getFont();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(EotharEchoEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(EotharEchoEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        Component name = Component.translatable("eco_de_eothar").withStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(0x94E4FF)).withBold(true).withItalic(true));
        renderLabel(name, 2.1D, pose, buffers, packedLight);
        if (entity.interactionReady()) {
            Component prompt = Component.translatable(
                    "luisb1202.functions.carga_lanas.9_gris_claro.atacrom.run_click_aqui.1")
                    .withStyle(ChatFormatting.ITALIC);
            renderLabel(prompt, 1.8D, pose, buffers, packedLight);
        }
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    private void renderLabel(Component text, double y, PoseStack pose, MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, y, 0.0D);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = pose.last().pose();
        float x = -font.width(text) / 2.0F;
        font.drawInBatch(text, x, 0.0F, 0xFFFFFFFF, false, matrix, buffers,
                Font.DisplayMode.NORMAL, 0x40000000, packedLight);
        pose.popPose();
    }
}