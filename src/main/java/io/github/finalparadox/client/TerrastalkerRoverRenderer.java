package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.TerrastalkerVisualState;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Exact compound renderer for the 18 visible armor-stand item parts in the
 * original `el_montura`. All 18 visible items use the same vanilla armor-stand
 * head/right-arm matrices directly, avoiding unstable interpolation state on
 * non-ticked proxy entities.
 */
public final class TerrastalkerRoverRenderer<E extends Entity & TerrastalkerVisualState>
        extends EntityRenderer<E> {
    private static final CabinPart[] FRIENDLY_CABIN = {
            new CabinPart(0.0D, 0.7D, 0.35D, 0.0F, 45.0F),
            new CabinPart(0.0D, 0.35D, 0.5D, 0.0F, 90.0F),
            new CabinPart(-0.5D, 0.35D, 0.0D, 90.0F, 90.0F),
            new CabinPart(0.5D, 0.35D, 0.0D, 270.0F, 90.0F),
            new CabinPart(0.0D, 0.0D, 0.35D, 0.0F, 135.0F),
            new CabinPart(-0.35D, 0.0D, 0.0D, 90.0F, 135.0F),
            new CabinPart(0.0D, 0.0D, -0.35D, 180.0F, 135.0F),
            new CabinPart(0.35D, 0.0D, 0.0D, 270.0F, 135.0F)
    };
    private static final CabinPart[] HOSTILE_CABIN = {
            new CabinPart(0.0D, 0.7D, 0.35D, 0.0F, 45.0F),
            new CabinPart(-0.35D, 0.7D, 0.0D, 90.0F, 45.0F),
            new CabinPart(0.0D, 0.7D, -0.35D, 180.0F, 45.0F),
            new CabinPart(0.35D, 0.7D, 0.0D, 270.0F, 45.0F),
            new CabinPart(0.0D, 0.35D, 0.5D, 0.0F, 90.0F),
            new CabinPart(-0.5D, 0.35D, 0.0D, 90.0F, 90.0F),
            new CabinPart(0.0D, 0.35D, -0.5D, 180.0F, 90.0F),
            new CabinPart(0.5D, 0.35D, 0.0D, 270.0F, 90.0F),
            new CabinPart(0.0D, 0.0D, 0.35D, 0.0F, 135.0F),
            new CabinPart(-0.35D, 0.0D, 0.0D, 90.0F, 135.0F),
            new CabinPart(0.0D, 0.0D, -0.35D, 180.0F, 135.0F),
            new CabinPart(0.35D, 0.0D, 0.0D, 270.0F, 135.0F)
    };

    /** Initial generation pose followed by source animation frames 5/10/15/20. */
    private static final LegPose[][] LEG_FRAMES = {
            {
                    LegPose.extended(45.0F), LegPose.extended(135.0F),
                    LegPose.extended(225.0F), LegPose.extended(315.0F)
            },
            {
                    LegPose.raised(40.0F), LegPose.extended(135.0F),
                    LegPose.raised(240.0F), LegPose.extended(315.0F)
            },
            {
                    LegPose.extended(15.0F), LegPose.extended(135.0F),
                    LegPose.extended(255.0F), LegPose.extended(315.0F)
            },
            {
                    LegPose.extended(45.0F), LegPose.raised(140.0F),
                    LegPose.extended(225.0F), LegPose.raised(320.0F)
            },
            {
                    LegPose.extended(45.0F), LegPose.extended(165.0F),
                    LegPose.extended(225.0F), LegPose.extended(285.0F)
            }
    };

    private static final int CANNON_PARTS = 2;
    private static final int LEG_PARTS = 8;
    private static final float SOURCE_ARMOR_STAND_LERP_TICKS = 3.0F;
    private static final float CONTINUOUS_RENDER_GAP_TICKS = 1.5F;
    private static final double[] CANNON_FORWARD = {1.29D, 0.86D};

    private static final ItemStack SLAB = new ItemStack(Items.SMOOTH_STONE_SLAB);
    private static final ItemStack CANNON = new ItemStack(Items.NETHERITE_BLOCK);
    private static final ItemStack UPPER_SHIELD = makeShield(false);
    private static final ItemStack FOOT_SHIELD = makeShield(true);
    private static final ItemStack HOSTILE_FOOT_SHIELD = makeShield(true, 14);

    private final EntityRenderDispatcher dispatcher;
    private final ItemRenderer itemRenderer;
    private final ArmorStandModel armorStandModel;
    private Level cachedLevel;
    private ArmorStand nameStand;
    private final Map<E, GaitTransition> gaitTransitions =
            new WeakHashMap<>();

    public TerrastalkerRoverRenderer(EntityRendererProvider.Context context) {
        super(context);
        dispatcher = context.getEntityRenderDispatcher();
        itemRenderer = context.getItemRenderer();
        armorStandModel = new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND));
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(E entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(
            E entity,
            float entityYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        ensureStands(entity.level());
        int partIndex = 0;
        float cabinYaw = Mth.rotLerp(
                partialTick, entity.getPreviousCabinYaw(), entity.getCabinYaw());
        float cannonPitch = Mth.lerp(
                partialTick, entity.getPreviousCannonPitch(), entity.getCannonPitch());
        float movementYaw = Mth.rotLerp(
                partialTick, entity.getPreviousMovementYaw(), entity.getMovementYaw());
        float turretYaw = Mth.rotLerp(
                partialTick, entity.getPreviousTurretYaw(), entity.getTurretYaw());

        CabinPart[] cabin = entity.isHostileVisual() ? HOSTILE_CABIN : FRIENDLY_CABIN;
        for (CabinPart part : cabin) {
            Vec3 offset = rotateLocal(part.x, part.y, part.z, cabinYaw);
            float yaw = cabinYaw + part.localYaw;
            renderHeadItem(entity, SLAB, offset, yaw, part.headPitch, false,
                    partIndex++, pose, buffers, packedLight);
        }

        // Turret and leg-core yaw are independent in the source construction.
        // The turret always follows the rider; firing locks only the rover's
        // movement/leg heading to its last accepted direction.
        for (double forward : CANNON_FORWARD) {
            Vec3 offset = rotateLocal(0.0D, 0.9D, forward, turretYaw);
            renderHeadItem(entity, CANNON, offset, turretYaw, cannonPitch, true,
                    partIndex++, pose, buffers, packedLight);
        }

        int frame = legFrameIndex(entity.getGaitFrame());
        float renderTick = entity.tickCount + partialTick;
        GaitTransition transition = gaitTransitions.computeIfAbsent(
                entity, ignored -> new GaitTransition(frame, renderTick));
        transition.advance(frame, renderTick);
        float gaitBlend = transition.blend(renderTick);
        float legDeployment = Mth.lerp(partialTick,
                entity.getPreviousLegDeployment(), entity.getLegDeployment());
        for (int legIndex = 0; legIndex < LEG_FRAMES[frame].length; legIndex++) {
            LegPose leg = LegPose.interpolateSpatial(
                    LEG_FRAMES[transition.fromFrame][legIndex],
                    LEG_FRAMES[transition.toFrame][legIndex], gaitBlend);
            if (legDeployment < 1.0F) {
                leg = LegPose.interpolateSpatial(
                        LegPose.raised(leg.angle), leg, legDeployment);
            }
            float yaw = movementYaw + leg.angle;

            Vec3 upperOffset = rotateLocal(
                    leg.upperX, leg.upperY, leg.upperZ, yaw);
            renderLegShield(entity, UPPER_SHIELD, upperOffset, yaw, leg.upperRoll,
                    partIndex++, pose, buffers, packedLight);

            Vec3 footOffset = rotateLocal(leg.footX, leg.footY, leg.footZ, yaw);
            renderLegShield(entity, entity.isHostileVisual() ? HOSTILE_FOOT_SHIELD : FOOT_SHIELD,
                    footOffset, yaw, leg.footRoll,
                    partIndex++, pose, buffers, packedLight);
        }

        if (partIndex != entity.sourceVisibleParts()) {
            throw new IllegalStateException("Rendered Terrastalker part count: " + partIndex);
        }

        if (entity.showMountHint() && entity.getFirstPassenger() == null) {
            renderMountHint(entity, cabinYaw, partialTick, pose, buffers, packedLight);
        }
        super.render(entity, entityYaw, partialTick, pose, buffers, packedLight);
    }

    private void renderMountHint(
            E entity,
            float cabinYaw,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        configurePart(nameStand);
        nameStand.setCustomName(Component.translatable(entity.isMeltingDown()
                ? "message.finalparadox.rover.exploding"
                : (entity.tickCount / 10 & 1) == 0
                ? "message.finalparadox.rover.mount_hint"
                : "message.finalparadox.rover.mount_hint.alt"));
        nameStand.setCustomNameVisible(true);
        Vec3 offset = new Vec3(0.0D, 0.9D, 0.0D);
        prepareForRender(nameStand, entity, offset, cabinYaw);
        renderStand(nameStand, offset, partialTick, pose, buffers, packedLight);
    }

    private void ensureStands(Level level) {
        if (cachedLevel == level && nameStand != null) return;
        cachedLevel = level;
        gaitTransitions.clear();
        nameStand = new ArmorStand(level, 0.0D, 0.0D, 0.0D);
    }

    private static void configurePart(ArmorStand stand) {
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
        stand.setCustomName(null);
        stand.setCustomNameVisible(false);

        CompoundTag flags = new CompoundTag();
        flags.putBoolean("Invisible", true);
        flags.putBoolean("Small", false);
        flags.putBoolean("ShowArms", false);
        flags.putBoolean("NoBasePlate", false);
        flags.putBoolean("Marker", true);
        stand.readAdditionalSaveData(flags);
        stand.setInvisible(true);
        stand.setShowArms(false);
        stand.setNoBasePlate(false);
    }

    private void prepareForRender(
            ArmorStand stand,
            E owner,
            Vec3 offset,
            float yaw
    ) {
        stand.setPos(owner.getX() + offset.x, owner.getY() + offset.y,
                owner.getZ() + offset.z);
        stand.setOldPosAndRot();
        stand.setYRot(yaw);
        stand.yRotO = yaw;
        stand.setYBodyRot(yaw);
        stand.yBodyRotO = yaw;
        stand.setYHeadRot(yaw);
        stand.yHeadRotO = yaw;
        stand.tickCount = owner.tickCount;
    }

    private void renderStand(
            ArmorStand stand,
            Vec3 offset,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        EntityRenderer<? super ArmorStand> renderer = dispatcher.getRenderer(stand);
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);
        renderer.render(stand, stand.getYRot(), partialTick, pose, buffers, packedLight);
        pose.popPose();
    }

    /** Replays the vanilla 1.20.1 armor-stand HEAD item transform chain. */
    private void renderHeadItem(
            E owner,
            ItemStack item,
            Vec3 offset,
            float yaw,
            float headPitch,
            boolean small,
            int sourcePartIndex,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);

        // LivingEntityRenderer and ArmorStandRenderer global transforms.
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.501F, 0.0F);

        // CustomHeadLayer's non-villager baby branch for small armor stands.
        if (small) {
            pose.translate(0.0F, 0.03125F, 0.0F);
            pose.scale(0.7F, 0.7F, 0.7F);
            pose.translate(0.0F, 1.0F, 0.0F);
        }

        // ArmorStandArmorModel.setupAnim for HeadPose:[pitch,0f,0f].
        armorStandModel.head.xRot = (float) Math.toRadians(headPitch);
        armorStandModel.head.yRot = 0.0F;
        armorStandModel.head.zRot = 0.0F;
        armorStandModel.head.translateAndRotate(pose);

        CustomHeadLayer.translateToHead(pose, false);
        itemRenderer.renderStatic(
                item,
                ItemDisplayContext.HEAD,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                pose,
                buffers,
                owner.level(),
                owner.getId() + sourcePartIndex);
        pose.popPose();
    }

    /**
     * Replays the exact vanilla 1.20.1 ArmorStandRenderer + ItemInHandLayer
     * transform chain for a full-size stand's right hand. Unlike a synthetic
     * ArmorStand entity, this matrix has no previous/current entity state that
     * can diverge before the rover is mounted.
     */
    private void renderLegShield(
            E owner,
            ItemStack shield,
            Vec3 offset,
            float yaw,
            float armRoll,
            int sourcePartIndex,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);

        // LivingEntityRenderer and ArmorStandRenderer global transforms.
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.501F, 0.0F);

        // ArmorStandArmorModel.setupAnim for RightArmPose:[90f,0f,roll].
        armorStandModel.rightArm.xRot = (float) Math.toRadians(90.0F);
        armorStandModel.rightArm.yRot = 0.0F;
        armorStandModel.rightArm.zRot = (float) Math.toRadians(armRoll);
        armorStandModel.translateToHand(HumanoidArm.RIGHT, pose);

        // ItemInHandLayer.renderArmWithItem for the right hand.
        pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.translate(1.0F / 16.0F, 0.125F, -0.625F);
        itemRenderer.renderStatic(
                shield,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                pose,
                buffers,
                owner.level(),
                owner.getId() + sourcePartIndex);
        pose.popPose();
    }

    private static Vec3 rotateLocal(double x, double y, double z, float yaw) {
        double radians = Math.toRadians(yaw);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new Vec3(x * cos - z * sin, y, x * sin + z * cos);
    }

    private static int legFrameIndex(int frame) {
        return frame < 0 || frame > 3 ? 0 : frame + 1;
    }

    private static ItemStack makeShield(boolean foot) {
        return makeShield(foot, 11);
    }

    private static ItemStack makeShield(boolean foot, int footColor) {
        ItemStack stack = new ItemStack(Items.SHIELD);
        CompoundTag blockEntity = new CompoundTag();
        blockEntity.putInt("Base", 8);
        ListTag patterns = new ListTag();
        addPattern(patterns, 7, "bri");
        addPattern(patterns, 8, "gru");
        addPattern(patterns, 8, "gra");
        addPattern(patterns, 8, "ss");
        if (foot) addPattern(patterns, footColor, "gra");
        blockEntity.put("Patterns", patterns);
        stack.getOrCreateTag().put("BlockEntityTag", blockEntity);
        return stack;
    }

    private static void addPattern(ListTag patterns, int color, String pattern) {
        CompoundTag entry = new CompoundTag();
        entry.putInt("Color", color);
        entry.putString("Pattern", pattern);
        patterns.add(entry);
    }

    private record CabinPart(double x, double y, double z, float localYaw, float headPitch) {
    }

    private record LegPose(
            float angle,
            double upperX,
            double upperY,
            double upperZ,
            double footX,
            double footY,
            double footZ,
            float upperRoll,
            float footRoll
    ) {
        private static LegPose extended(float angle) {
            return new LegPose(angle,
                    -0.6D, -0.45D, 0.35D,
                    -1.2D, -0.8D, 0.35D,
                    100.0F, 20.0F);
        }

        private static LegPose raised(float angle) {
            return new LegPose(angle,
                    -0.6D, -0.1D, 0.45D,
                    -0.6D, -0.1D, 0.45D,
                    130.0F, -10.0F);
        }

        /**
         * Recreates the three client ticks of positional/yaw interpolation that
         * the source's networked armor stands received after each teleport.
         * Right-arm pose NBT changed immediately in the source and stays discrete.
         */
        private static LegPose interpolateSpatial(
                LegPose from, LegPose to, float blend) {
            return new LegPose(
                    Mth.rotLerp(blend, from.angle, to.angle),
                    Mth.lerp(blend, from.upperX, to.upperX),
                    Mth.lerp(blend, from.upperY, to.upperY),
                    Mth.lerp(blend, from.upperZ, to.upperZ),
                    Mth.lerp(blend, from.footX, to.footX),
                    Mth.lerp(blend, from.footY, to.footY),
                    Mth.lerp(blend, from.footZ, to.footZ),
                    to.upperRoll, to.footRoll);
        }
    }

    private static final class GaitTransition {
        private int fromFrame;
        private int toFrame;
        private float startTick;
        private float lastRenderTick;

        private GaitTransition(int frame, float renderTick) {
            fromFrame = frame;
            toFrame = frame;
            startTick = renderTick - SOURCE_ARMOR_STAND_LERP_TICKS;
            lastRenderTick = renderTick;
        }

        private void advance(int targetFrame, float renderTick) {
            if (targetFrame != toFrame) {
                if (renderTick - lastRenderTick > CONTINUOUS_RENDER_GAP_TICKS) {
                    fromFrame = targetFrame;
                    toFrame = targetFrame;
                    startTick = renderTick - SOURCE_ARMOR_STAND_LERP_TICKS;
                } else {
                    fromFrame = toFrame;
                    toFrame = targetFrame;
                    startTick = renderTick;
                }
            }
            lastRenderTick = renderTick;
        }

        private float blend(float renderTick) {
            return Mth.clamp(
                    (renderTick - startTick) / SOURCE_ARMOR_STAND_LERP_TICKS,
                    0.0F, 1.0F);
        }
    }
}
