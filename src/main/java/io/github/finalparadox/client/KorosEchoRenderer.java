package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.KorosEchoEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;

/** Renders the six dropped-block visuals and two floating labels from minikoros/gen2. */
public final class KorosEchoRenderer extends EntityRenderer<KorosEchoEntity> {
    private static final ItemStack CORE = new ItemStack(Blocks.SEA_LANTERN);
    private static final ItemStack SLAB = new ItemStack(Blocks.QUARTZ_SLAB);

    private final ItemRenderer items;
    private final Font font;

    public KorosEchoRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        font = context.getFont();
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(KorosEchoEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(KorosEchoEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        float rotation = (entity.tickCount + partialTick) * 2.0F;
        renderItem(entity, CORE, 0.0D, 1.0D, 0.0D, rotation, 0, pose, buffers, packedLight);
        renderItem(entity, SLAB, 0.433D, 1.0D, 0.25D, rotation, 1, pose, buffers, packedLight);
        renderItem(entity, SLAB, -0.433D, 1.0D, 0.25D, rotation, 2, pose, buffers, packedLight);
        renderItem(entity, SLAB, 0.0D, 1.0D, -0.5D, rotation, 3, pose, buffers, packedLight);
        renderItem(entity, SLAB, 0.0D, 1.6D, 0.0D, rotation, 4, pose, buffers, packedLight);
        renderItem(entity, SLAB, 0.0D, 0.5D, 0.0D, rotation, 5, pose, buffers, packedLight);

        Component name = Component.translatable("entity.finalparadox.koros_echo.name").withStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF)).withBold(true).withItalic(true));
        String promptKey = (entity.tickCount / 10) % 2 == 0
                ? "entity.finalparadox.koros_echo.prompt.spaced"
                : "entity.finalparadox.koros_echo.prompt.compact";
        Component prompt = Component.translatable(promptKey).withStyle(ChatFormatting.ITALIC);
        renderLabel(name, 2.15D, pose, buffers, packedLight);
        renderLabel(prompt, 1.85D, pose, buffers, packedLight);
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    private void renderItem(KorosEchoEntity entity, ItemStack stack, double x, double y, double z,
                            float rotation, int seedOffset, PoseStack pose, MultiBufferSource buffers,
                            int packedLight) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        items.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                pose, buffers, entity.level(), entity.getId() + seedOffset);
        pose.popPose();
    }

    private void renderLabel(Component text, double y, PoseStack pose, MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, y, 0.0D);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = pose.last().pose();
        float x = -font.width(text) / 2.0F;
        font.drawInBatch(text, x, 0.0F, 0xFFFFFFFF, false, matrix, buffers,
                Font.DisplayMode.NORMAL, 0x40000000, packedLight);
        pose.popPose();
    }
}
