package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.B8DetonatorBombEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;

/** Draws the source armor-stand pair as one rotating TNT plus floating timer. */
public final class B8DetonatorBombRenderer extends EntityRenderer<B8DetonatorBombEntity> {
    private final BlockRenderDispatcher blocks;
    private final Font font;

    public B8DetonatorBombRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        font = context.getFont();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(B8DetonatorBombEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(B8DetonatorBombEntity entity, float entityYaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int packedLight) {
        int fullBright = LightTexture.FULL_BRIGHT;
        pose.pushPose();
        pose.translate(0.0D, 0.55D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 10.0F));
        pose.scale(0.7F, 0.7F, 0.7F);
        pose.translate(-0.5D, -0.5D, -0.5D);
        blocks.renderSingleBlock(Blocks.TNT.defaultBlockState(),
                pose, buffers, fullBright, OverlayTexture.NO_OVERLAY);
        pose.popPose();

        int number = entity.getCountdownNumber();
        String suffix = entity.isRedFlash() ? ".2" : ".1";
        Component timer = Component.translatable(
                "luisb1202.functions.afijos.detonante." + number + suffix);
        pose.pushPose();
        pose.translate(0.0D, 1.15D, 0.0D);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = pose.last().pose();
        font.drawInBatch(timer, -font.width(timer) / 2.0F, 0.0F,
                0xFFFFFFFF, false, matrix, buffers, Font.DisplayMode.NORMAL,
                0x40000000, fullBright);
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffers, packedLight);
    }
}
