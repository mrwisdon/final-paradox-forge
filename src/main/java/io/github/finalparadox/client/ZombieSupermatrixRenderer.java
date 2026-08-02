package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.ZombieSupermatrixEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/** Exact compound renderer for the armor-stand construction in b8/matriz/gen. */
public final class ZombieSupermatrixRenderer extends EntityRenderer<ZombieSupermatrixEntity> {
    private static final MatrixRing[] GOLD_RINGS = {
            // run_* first applies positioned ~ ~-0.3 ~, then these local offsets.
            new MatrixRing(6, 60.0F, 90.0F, -1.0F,
                    0.8D, -0.3D, 2.4D, -0.3D),
            new MatrixRing(4, 90.0F, 45.0F, 1.0F,
                    0.56D, 0.26D, 1.68D, 1.38D),
            new MatrixRing(4, 90.0F, 45.0F, 1.0F,
                    0.56D, -1.2D, 1.68D, -2.5D)
    };

    private static final ItemStack GOLD_BLOCK = new ItemStack(Items.GOLD_BLOCK);
    private static final ItemStack SEA_LANTERN = new ItemStack(Items.SEA_LANTERN);
    private static final int GOLD_PARTS = 6 + 4 + 4;
    private static final int HELMET_PARTS = GOLD_PARTS + 1;
    private static final float SOURCE_ARMOR_STAND_LERP_TICKS = 3.0F;

    static {
        if (GOLD_PARTS != ZombieSupermatrixEntity.SOURCE_GOLD_PARTS
                || HELMET_PARTS != ZombieSupermatrixEntity.SOURCE_HELMET_PARTS) {
            throw new IllegalStateException("Zombie Supermatrix source part count mismatch");
        }
    }

    private final EntityRenderDispatcher dispatcher;
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderer;
    private final ArmorStandModel armorStandModel;
    private Level cachedLevel;
    private ArmorStand nameStand;
    private final Map<ZombieSupermatrixEntity, ExpansionTransition> expansionTransitions =
            new WeakHashMap<>();

    public ZombieSupermatrixRenderer(EntityRendererProvider.Context context) {
        super(context);
        dispatcher = context.getEntityRenderDispatcher();
        itemRenderer = context.getItemRenderer();
        blockRenderer = context.getBlockRenderDispatcher();
        armorStandModel = new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND));
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieSupermatrixEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(
            ZombieSupermatrixEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        boolean vulnerable = entity.isVulnerableModel();
        float renderTick = entity.tickCount + partialTick;
        ExpansionTransition expansion = expansionTransitions.computeIfAbsent(
                entity, ignored -> new ExpansionTransition(vulnerable, renderTick));
        expansion.advance(vulnerable, renderTick);
        float openAmount = expansion.sample(renderTick);
        float sourceSpin = entity.spinTicks(partialTick) * 3.0F;
        int partIndex = 0;

        renderSourceLantern(pose, buffers, packedLight);

        for (MatrixRing ring : GOLD_RINGS) {
            double radius = Mth.lerp(openAmount, ring.compactRadius, ring.vulnerableRadius);
            double y = Mth.lerp(openAmount, ring.compactY, ring.vulnerableY);
            for (int index = 0; index < ring.count; index++) {
                float yaw = index * ring.yawStep + sourceSpin * ring.spinDirection;
                // In the command, local ^^^ position is resolved from the old
                // stand yaw before tp applies the new relative yaw for this tick.
                float positionYaw = yaw - 3.0F * ring.spinDirection;
                Vec3 offset = rotateLocal(0.0D, y, radius, positionYaw);
                renderHeadItem(entity, GOLD_BLOCK, offset, yaw, ring.headPitch,
                        partIndex++, pose, buffers, packedLight);
            }
        }

        // b8_matriz_hitbox: full-size sea-lantern helmet at the steady-state
        // run_* origin y=-0.5, rotating +3 degrees each tick.
        renderHeadItem(entity, SEA_LANTERN, new Vec3(0.0D, -0.5D, 0.0D),
                sourceSpin, 0.0F, partIndex++, pose, buffers, packedLight);

        if (partIndex != ZombieSupermatrixEntity.SOURCE_HELMET_PARTS) {
            throw new IllegalStateException("Rendered Zombie Supermatrix helmet parts: " + partIndex);
        }

        renderSourceName(entity, Mth.lerp(openAmount, 2.7D, 4.7D),
                partialTick, pose, buffers, packedLight);
        super.render(entity, entityYaw, partialTick, pose, buffers, packedLight);
    }

    /** b8/matriz/gen places one upright world lantern at core + (0,1,0). */
    private void renderSourceLantern(PoseStack pose, MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, 1.0D, 0.0D);
        blockRenderer.renderSingleBlock(Blocks.LANTERN.defaultBlockState(), pose, buffers,
                packedLight, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    /** Replays the vanilla 1.20.1 full-size armor-stand HEAD item transform. */
    private void renderHeadItem(
            ZombieSupermatrixEntity owner,
            ItemStack item,
            Vec3 offset,
            float yaw,
            float headPitch,
            int sourcePartIndex,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);

        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.501F, 0.0F);

        armorStandModel.head.xRot = (float) Math.toRadians(headPitch);
        armorStandModel.head.yRot = 0.0F;
        armorStandModel.head.zRot = 0.0F;
        armorStandModel.head.translateAndRotate(pose);

        CustomHeadLayer.translateToHead(pose, false);
        itemRenderer.renderStatic(item, ItemDisplayContext.HEAD, packedLight,
                OverlayTexture.NO_OVERLAY, pose, buffers, owner.level(),
                owner.getId() + sourcePartIndex);
        pose.popPose();
    }

    private void renderSourceName(
            ZombieSupermatrixEntity owner,
            double y,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        ensureNameStand(owner.level());
        configureNameStand(nameStand);
        nameStand.setCustomName(Component.translatable("entity.finalparadox.zombie_supermatrix.name")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        nameStand.setCustomNameVisible(true);
        nameStand.setPos(owner.getX(), owner.getY() + y, owner.getZ());
        nameStand.setOldPosAndRot();
        nameStand.tickCount = owner.tickCount;

        EntityRenderer<? super ArmorStand> renderer = dispatcher.getRenderer(nameStand);
        pose.pushPose();
        pose.translate(0.0D, y, 0.0D);
        renderer.render(nameStand, 0.0F, partialTick, pose, buffers, packedLight);
        pose.popPose();
    }

    private void ensureNameStand(Level level) {
        if (cachedLevel == level && nameStand != null) {
            return;
        }
        cachedLevel = level;
        expansionTransitions.clear();
        nameStand = new ArmorStand(level, 0.0D, 0.0D, 0.0D);
    }

    private static void configureNameStand(ArmorStand stand) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stand.setItemSlot(slot, ItemStack.EMPTY);
        }
        Rotations zero = new Rotations(0.0F, 0.0F, 0.0F);
        stand.setHeadPose(zero);
        stand.setBodyPose(zero);
        stand.setLeftArmPose(zero);
        stand.setRightArmPose(zero);
        stand.setLeftLegPose(zero);
        stand.setRightLegPose(zero);

        CompoundTag flags = new CompoundTag();
        flags.putBoolean("Invisible", true);
        flags.putBoolean("Small", true);
        flags.putBoolean("Marker", true);
        stand.readAdditionalSaveData(flags);
        stand.setInvisible(true);
    }

    private static Vec3 rotateLocal(double x, double y, double z, float yaw) {
        double radians = Math.toRadians(yaw);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new Vec3(x * cos - z * sin, y, x * sin + z * cos);
    }

    private record MatrixRing(
            int count,
            float yawStep,
            float headPitch,
            float spinDirection,
            double compactRadius,
            double compactY,
            double vulnerableRadius,
            double vulnerableY
    ) {
    }

    /** Mirrors the three client ticks used to interpolate source armor-stand teleports. */
    private static final class ExpansionTransition {
        private boolean vulnerable;
        private float from;
        private float to;
        private float startTick;

        private ExpansionTransition(boolean vulnerable, float renderTick) {
            this.vulnerable = vulnerable;
            from = vulnerable ? 1.0F : 0.0F;
            to = from;
            startTick = renderTick;
        }

        private void advance(boolean nextVulnerable, float renderTick) {
            if (nextVulnerable == vulnerable) {
                return;
            }
            from = sample(renderTick);
            to = nextVulnerable ? 1.0F : 0.0F;
            startTick = renderTick;
            vulnerable = nextVulnerable;
        }

        private float sample(float renderTick) {
            float progress = Mth.clamp(
                    (renderTick - startTick) / SOURCE_ARMOR_STAND_LERP_TICKS,
                    0.0F, 1.0F);
            return Mth.lerp(progress, from, to);
        }
    }
}
