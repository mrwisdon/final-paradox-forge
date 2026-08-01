package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.GreatHookEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class GreatHookRenderer extends EntityRenderer<GreatHookEntity> {
    private final BlockRenderDispatcher blocks;
    public GreatHookRenderer(EntityRendererProvider.Context context){super(context);blocks=context.getBlockRenderDispatcher();shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(GreatHookEntity entity){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(GreatHookEntity hook,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){Entity owner=hook.ownerForRender();if(owner==null)return;Vec3 from=owner.getEyePosition(partial).subtract(hook.getPosition(partial));double length=from.length();if(length<.01)return;Vec3 unit=from.scale(1/length);float yRot=(float)Math.toDegrees(Math.atan2(unit.x,unit.z));float xRot=(float)-Math.toDegrees(Math.asin(unit.y));for(double d=.35;d<length;d+=.52){pose.pushPose();pose.translate(unit.x*d,unit.y*d,unit.z*d);pose.mulPose(Axis.YP.rotationDegrees(yRot));pose.mulPose(Axis.XP.rotationDegrees(xRot+90));pose.scale(.42F,.42F,.42F);pose.translate(-.5,-.5,-.5);blocks.renderSingleBlock(Blocks.CHAIN.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();}pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(yRot));pose.mulPose(Axis.XP.rotationDegrees(xRot));pose.scale(.75F,.75F,.75F);pose.translate(-.5,-.3,-.5);blocks.renderSingleBlock(Blocks.HOPPER.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();super.render(hook,yaw,partial,pose,buffers,light);}
}
