package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.PaladinHammerSwingEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class PaladinHammerSwingRenderer extends EntityRenderer<PaladinHammerSwingEntity>{
    private final BlockRenderDispatcher blocks;private final ItemRenderer items;
    public PaladinHammerSwingRenderer(EntityRendererProvider.Context context){super(context);blocks=context.getBlockRenderDispatcher();items=context.getItemRenderer();shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(PaladinHammerSwingEntity entity){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(PaladinHammerSwingEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
        pose.pushPose();pose.translate(0.55,-0.6,1.15);pose.mulPose(Axis.XP.rotationDegrees(90));pose.mulPose(Axis.YP.rotationDegrees(45));pose.scale(2.2F,2.2F,2.2F);items.renderStatic(new ItemStack(Items.BLAZE_ROD),ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId());pose.popPose();
        renderGold(pose,buffers,light,0.5,-0.7,2.0,1.0F);renderGold(pose,buffers,light,0.3,0.2,2.2,0.7F);renderGold(pose,buffers,light,1.2,-0.7,2.0,1.0F);
        pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);
    }
    private void renderGold(PoseStack pose,MultiBufferSource buffers,int light,double x,double y,double z,float scale){pose.pushPose();pose.translate(x,y,z);pose.mulPose(Axis.YP.rotationDegrees(45));pose.scale(scale,scale,scale);pose.translate(-0.5,-0.5,-0.5);blocks.renderSingleBlock(Blocks.GOLD_BLOCK.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();}
}
