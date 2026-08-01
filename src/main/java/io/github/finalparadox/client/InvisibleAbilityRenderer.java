package io.github.finalparadox.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;

public final class InvisibleAbilityRenderer<T extends Entity> extends EntityRenderer<T> {
    public InvisibleAbilityRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=0;}
    @Override public ResourceLocation getTextureLocation(T entity){return InventoryMenu.BLOCK_ATLAS;}
}
