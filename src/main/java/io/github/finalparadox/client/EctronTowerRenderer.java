package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.EctronTowerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class EctronTowerRenderer extends EntityRenderer<EctronTowerEntity> {
    private final BlockRenderDispatcher blocks;
    public EctronTowerRenderer(EntityRendererProvider.Context context) { super(context); blocks = context.getBlockRenderDispatcher(); shadowRadius = .7F; }
    @Override public ResourceLocation getTextureLocation(EctronTowerEntity entity) { return InventoryMenu.BLOCK_ATLAS; }

    @Override public void render(EctronTowerEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, EctronTowerEntity.appearanceOffset(entity.tickCount), 0);
        block(pose,buffers,light,Blocks.SMOOTH_STONE_SLAB,0,2.7,0,.625F,0);
        for(double y:new double[]{-.6,0,.6,1.2,1.8}){
            block(pose,buffers,light,Blocks.IRON_BARS,-.3,y,0,.625F,90);
            block(pose,buffers,light,Blocks.IRON_BARS,0,y,.3,.625F,0);
        }
        block(pose,buffers,light,Blocks.GLASS,0,2.95,0,1,0);
        block(pose,buffers,0xF000F0,Blocks.SEA_LANTERN,0,3.75,0,.625F,(entity.tickCount+partial)*5);
        Block[] carpets={Blocks.YELLOW_CARPET,Blocks.BLACK_CARPET,Blocks.YELLOW_CARPET,Blocks.BLACK_CARPET,Blocks.YELLOW_CARPET,Blocks.BLACK_CARPET};
        for(int i=0;i<6;i++)block(pose,buffers,light,carpets[i],0,3.3-i*.05,0,.625F,i*60);
        block(pose,buffers,light,Blocks.SMOOTH_STONE_SLAB,0,2.65,0,1,0);
        pose.popPose();
        super.render(entity,yaw,partial,pose,buffers,light);
    }

    private void block(PoseStack pose,MultiBufferSource buffers,int light,Block block,double x,double y,double z,float scale,double yaw){pose.pushPose();pose.translate(x,y,z);pose.mulPose(Axis.YP.rotationDegrees((float)yaw));pose.translate(-scale/2,0,-scale/2);pose.scale(scale,scale,scale);blocks.renderSingleBlock(block.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();}
}
