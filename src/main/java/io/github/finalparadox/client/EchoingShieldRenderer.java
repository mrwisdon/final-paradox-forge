package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.EchoingShieldEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

public final class EchoingShieldRenderer extends EntityRenderer<EchoingShieldEntity> {
    private static final double[][] HELD={{0,-.26},{-.455,0},{-.455,.53},{0,.8},{.455,.53},{.455,0}};
    private static final double[][] THROWN={{0,-.52},{.45,-.25},{.45,.25},{0,.52},{-.45,.25},{-.45,-.25}};
    private final BlockRenderDispatcher blocks;
    public EchoingShieldRenderer(EntityRendererProvider.Context context){super(context);blocks=context.getBlockRenderDispatcher();shadowRadius=.3F;}
    @Override public ResourceLocation getTextureLocation(EchoingShieldEntity entity){return InventoryMenu.BLOCK_ATLAS;}

    @Override public void render(EchoingShieldEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        for(int i=0;i<6;i++){
            pose.pushPose();
            if(entity.held()){
                pose.translate(HELD[i][0],HELD[i][1],0);
                pose.mulPose(Axis.XP.rotationDegrees(20));
                pose.mulPose(Axis.ZP.rotationDegrees(i*60));
            }else{
                pose.translate(THROWN[i][0],0,THROWN[i][1]);
                pose.mulPose(Axis.XP.rotationDegrees(110));
                pose.mulPose(Axis.YP.rotationDegrees(i*60));
            }
            pose.scale(.625F,.625F,.18F);
            pose.translate(-.5,-.5,-.5);
            blocks.renderSingleBlock(Blocks.MAGENTA_STAINED_GLASS_PANE.defaultBlockState(),pose,buffers,0xF000F0,OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
        pose.popPose();
        super.render(entity,yaw,partial,pose,buffers,light);
    }
}
