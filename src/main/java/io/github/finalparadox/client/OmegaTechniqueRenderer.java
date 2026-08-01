package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.OmegaTechniqueEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public final class OmegaTechniqueRenderer extends EntityRenderer<OmegaTechniqueEntity>{private final ItemRenderer items;public OmegaTechniqueRenderer(EntityRendererProvider.Context c){super(c);items=c.getItemRenderer();shadowRadius=0;}@Override public ResourceLocation getTextureLocation(OmegaTechniqueEntity e){return InventoryMenu.BLOCK_ATLAS;}@Override public void render(OmegaTechniqueEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){float age=e.tickCount+partial;pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-e.getYRot()));if(e.mode()==OmegaTechniqueEntity.DASH){pose.translate(.5,-.1,2.5);pose.mulPose(Axis.XP.rotationDegrees(-90));}else if(age<7){pose.translate(0,-.3,2);pose.mulPose(Axis.YP.rotationDegrees(100-age*28));pose.translate(3,0,0);pose.mulPose(Axis.ZP.rotationDegrees(60));}else{pose.translate(0,-.25,Math.min(6,(age-7)*1.5));pose.mulPose(Axis.XP.rotationDegrees(-90));}pose.scale(1.4F,1.4F,1.4F);items.renderStatic(e.displayStack(),ItemDisplayContext.FIXED,15728880,OverlayTexture.NO_OVERLAY,pose,buffers,e.level(),e.getId());pose.popPose();super.render(e,yaw,partial,pose,buffers,light);}}
