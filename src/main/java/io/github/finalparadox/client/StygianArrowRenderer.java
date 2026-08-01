package io.github.finalparadox.client;

import io.github.finalparadox.entity.StygianArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.resources.ResourceLocation;

public final class StygianArrowRenderer extends ArrowRenderer<StygianArrowEntity> {
    public StygianArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(StygianArrowEntity entity) {
        return TippableArrowRenderer.NORMAL_ARROW_LOCATION;
    }
}
