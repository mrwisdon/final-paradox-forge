package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.ElectricHammerSwingEntity;
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

public final class ElectricHammerSwingRenderer extends EntityRenderer<ElectricHammerSwingEntity> {
    private final BlockRenderDispatcher blocks; private final ItemRenderer items;
    public ElectricHammerSwingRenderer(EntityRendererProvider.Context context){super(context);blocks=context.getBlockRenderDispatcher();items=context.getItemRenderer();shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(ElectricHammerSwingEntity entity){return InventoryMenu.BLOCK_ATLAS;}
    @Override public void render(ElectricHammerSwingEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){int glow=0xF000F0;pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));pose.pushPose();pose.translate(.55,-.6,1.15);pose.mulPose(Axis.XP.rotationDegrees(90));pose.mulPose(Axis.YP.rotationDegrees(45));pose.scale(2.2F,2.2F,2.2F);items.renderStatic(new ItemStack(Items.STICK),ItemDisplayContext.FIXED,glow,OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId());pose.popPose();block(pose,buffers,glow,.5,-.7,2,1);block(pose,buffers,glow,.3,.2,2.2,.7F);block(pose,buffers,glow,1.2,-.7,2,1);pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);}
    private void block(PoseStack pose,MultiBufferSource buffers,int light,double x,double y,double z,float scale){pose.pushPose();pose.translate(x,y,z);pose.mulPose(Axis.YP.rotationDegrees(45));pose.scale(scale,scale,scale);pose.translate(-.5,-.5,-.5);blocks.renderSingleBlock(Blocks.BLACK_CONCRETE.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);pose.popPose();}
}
