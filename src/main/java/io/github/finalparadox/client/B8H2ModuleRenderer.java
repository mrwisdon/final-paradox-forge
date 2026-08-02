package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.B8H2ModuleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the H2 gold-block module. The entity origin matches the source
 * armor-stand origin; the block uses the vanilla full-size armor-stand head
 * placement and follows the source teleport/rotation animation.
 */
public final class B8H2ModuleRenderer extends EntityRenderer<B8H2ModuleEntity> {
    private static final double SOURCE_HEAD_CENTER_Y = 1.6875D;
    private static final float SOURCE_HEAD_ITEM_SCALE = 0.625F;

    private final BlockRenderDispatcher blocks;

    public B8H2ModuleRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(B8H2ModuleEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(B8H2ModuleEntity entity, float entityYaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();

        // Source gen.mcfunction summons at the matrix core, then teleports the
        // armor stand to its selected sky position. Vanilla renders that long
        // teleport as a three-tick linear throw. Keep the server hit position
        // at the destination and reproduce only the original visual offset.
        float launchProgress = entity.launchProgress(partialTick);
        entity.launchOrigin().ifPresent(origin -> {
            if (launchProgress < 1.0F) {
                Vec3 current = entity.getPosition(partialTick);
                double remaining = 1.0D - launchProgress;
                pose.translate(
                        (origin.x - current.x) * remaining,
                        (origin.y - current.y) * remaining,
                        (origin.z - current.z) * remaining);
            }
        });

        // Match the full-size armor-stand head item: a 0.625-scale block
        // centered at head height. Rotate around the block center so it cannot
        // orbit or wobble as the source armor stand turns three degrees/tick.
        pose.translate(0.0D, SOURCE_HEAD_CENTER_Y, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(-entity.moduleYaw(partialTick)));
        pose.scale(SOURCE_HEAD_ITEM_SCALE, SOURCE_HEAD_ITEM_SCALE, SOURCE_HEAD_ITEM_SCALE);
        pose.translate(-0.5D, -0.5D, -0.5D);
        blocks.renderSingleBlock(Blocks.GOLD_BLOCK.defaultBlockState(),
                pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, light);
    }
}
