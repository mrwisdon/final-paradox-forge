package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.BamboomerangEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public final class BamboomerangRenderer extends EntityRenderer<BamboomerangEntity> {
    private final ItemRenderer items;
    public BamboomerangRenderer(EntityRendererProvider.Context context){super(context);items=context.getItemRenderer();shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(BamboomerangEntity entity){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(BamboomerangEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){renderHalf(entity,pose,buffers,light,entity.getYRot(),1.3);renderHalf(entity,pose,buffers,light,entity.getYRot()+180,-1.3);super.render(entity,yaw,partial,pose,buffers,light);}
    private void renderHalf(BamboomerangEntity entity,PoseStack pose,MultiBufferSource buffers,int light,float spin,double offset){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-spin));pose.translate(offset,0,-1);pose.mulPose(Axis.XP.rotationDegrees(90));pose.mulPose(Axis.ZP.rotationDegrees(90));pose.scale(1.1F,1.1F,1.1F);items.renderStatic(entity.displayStack(),ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId()+(int)offset);pose.popPose();}
}
