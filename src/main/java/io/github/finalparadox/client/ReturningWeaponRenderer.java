package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.HarvesterEntity;
import io.github.finalparadox.entity.ReturningWeaponEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public final class ReturningWeaponRenderer<T extends ReturningWeaponEntity> extends EntityRenderer<T> {
    private final ItemRenderer items;
    public ReturningWeaponRenderer(EntityRendererProvider.Context context){super(context);items=context.getItemRenderer();shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(T entity){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(T entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){if(entity.displayStack().isEmpty())return;float spin=entity.getYRot();if(entity instanceof HarvesterEntity){renderOne(entity,pose,buffers,light,spin,0.7D);renderOne(entity,pose,buffers,light,spin+180,-0.7D);}else renderOne(entity,pose,buffers,light,spin,0);super.render(entity,yaw,partial,pose,buffers,light);}
    private void renderOne(T entity,PoseStack pose,MultiBufferSource buffers,int light,float spin,double offset){pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-spin));pose.translate(offset,0,0);pose.mulPose(Axis.XP.rotationDegrees(90));pose.mulPose(Axis.ZP.rotationDegrees(90));pose.scale(1.25F,1.25F,1.25F);items.renderStatic(entity.displayStack(),ItemDisplayContext.FIXED,light,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId()+(int)(offset*10));pose.popPose();}
}
