package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.VoidSingularityEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

public final class VoidSingularityRenderer extends EntityRenderer<VoidSingularityEntity> {
    private final BlockRenderDispatcher blocks;
    public VoidSingularityRenderer(EntityRendererProvider.Context context) { super(context); blocks=context.getBlockRenderDispatcher(); shadowRadius=0; }
    @Override public ResourceLocation getTextureLocation(VoidSingularityEntity entity) { return InventoryMenu.BLOCK_ATLAS; }
    @Override public void render(VoidSingularityEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose(); float spin=(entity.tickCount+partial)*18F; pose.translate(0,.15,0); pose.mulPose(Axis.YP.rotationDegrees(spin)); pose.mulPose(Axis.XP.rotationDegrees(45)); pose.scale(.7F,.25F,.7F); pose.translate(-.5,-.5,-.5); blocks.renderSingleBlock(Blocks.BLACK_CONCRETE.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY); pose.popPose(); super.render(entity,yaw,partial,pose,buffers,light);
    }
}
