package io.github.finalparadox.client;

import io.github.finalparadox.entity.DroneBodyProxyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/** Renders the anchor proxy as the owner's player skin and equipment. */
public final class DroneBodyProxyRenderer extends LivingEntityRenderer<
        DroneBodyProxyEntity, HumanoidModel<DroneBodyProxyEntity>> {

    public DroneBodyProxyRenderer(EntityRendererProvider.Context context) {
        super(context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(DroneBodyProxyEntity entity) {
        UUID ownerId = entity.getOwnerId();
        if (ownerId == null) {
            return DefaultPlayerSkin.getDefaultSkin();
        }
        if (Minecraft.getInstance().getConnection() != null) {
            PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(ownerId);
            if (info != null) {
                return info.getSkinLocation();
            }
        }
        return DefaultPlayerSkin.getDefaultSkin(ownerId);
    }
}
