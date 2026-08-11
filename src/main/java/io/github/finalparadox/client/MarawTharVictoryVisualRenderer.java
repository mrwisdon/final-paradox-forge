package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.finalparadox.entity.MarawTharBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Recreates the victory actors as one client-side compound visual.
 *
 * <p>The map built both actors from armor stands. Using virtual, untracked armor stands here
 * preserves vanilla armor/head/hand slot transforms without adding network entities.</p>
 */
final class MarawTharVictoryVisualRenderer {
    static final int STATUE_SPAWN_TICK = 36 * 20;
    static final int STATUE_END_TICK = 63 * 20;

    private static final int PETRIFICATION_START_TICK = 50 * 20;
    private static final int STATUE_FALL_START_TICK = 59 * 20;
    private static final double STATUE_NAME_Y = 3.3D;
    private static final float SOURCE_YAW = 90.0F;
    private static final int HUMAN_STAND_COUNT = 4;
    private static final int STATUE_PART_COUNT = 15;

    private static final int[] PART_FALL_DELAYS = {
            20, 24, 28, 32, 36, 36, 40, 44, 48, 48, 8, 6, 4, 2, 0
    };
    private static final boolean[] PART_SMALL = {
            false, false, false, false, false, false, false, false, false, false,
            false, true, true, true, true
    };
    private static final boolean[] PART_HELD_IN_RIGHT_HAND = {
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, true, true
    };
    private static final double[][] PART_LOCAL_OFFSETS = {
            {0.0D, -0.45D, 0.5D}, {0.0D, -0.45D, -0.5D},
            {0.18D, 0.12D, -1.05D}, {0.18D, 0.12D, 1.05D},
            {0.0D, -0.5D, 0.0D}, {0.1D, -0.7D, 0.0D},
            {0.2D, 0.9D, 0.7D}, {0.2D, 0.9D, -0.7D},
            {0.0D, 0.8D, 0.0D}, {-0.45D, 1.3D, 0.0D},
            {-0.6D, 0.35D, -0.025D}, {-0.5D, 1.25D, -0.4D},
            {-0.5D, 1.1D, 0.3D}, {0.1D, 1.45D, -0.25D},
            {0.1D, 1.75D, 0.45D}
    };
    private static final float[][] PART_POSES = {
            {45.0F, -20.0F, 0.0F}, {45.0F, 200.0F, 0.0F},
            {135.0F, 20.0F, 0.0F}, {135.0F, 160.0F, 0.0F},
            {0.0F, 180.0F, 0.0F}, {0.0F, 270.0F, -10.0F},
            {45.0F, 160.0F, 0.0F}, {45.0F, 20.0F, 0.0F},
            {0.0F, 180.0F, 0.0F}, {180.0F, 270.0F, 10.0F},
            {0.0F, 90.0F, 0.0F}, {0.0F, 90.0F, 0.0F},
            {0.0F, 90.0F, 0.0F}, {-90.0F, 90.0F, 0.0F},
            {-90.0F, 90.0F, 0.0F}
    };
    private static final int[] BLINK_INTERVALS = {30, 5, 3, 30, 5, 30, 3, 5};

    private final EntityRenderDispatcher dispatcher;
    private Level cachedLevel;
    private ArmorStand[] humanStands;
    private ArmorStand[] statueParts;
    private ArmorStand nameStand;

    MarawTharVictoryVisualRenderer(EntityRendererProvider.Context context) {
        dispatcher = context.getEntityRenderDispatcher();
    }

    void renderHuman(
            MarawTharBossEntity owner,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        ensureStands(owner.level());
        int tick = owner.getVictoryTick();
        int stage = tick >= 18 * 20 ? 3 : tick >= 16 * 20 ? 2 : tick >= 11 * 20 ? 1 : 0;

        for (ArmorStand stand : humanStands) {
            resetEquipment(stand);
            resetPose(stand);
            prepareForRender(stand, owner, 0.0D, 0.0D, 0.0D, SOURCE_YAW, tick);
        }

        ArmorStand main = humanStands[0];
        configureArmorStand(main, false, false, true);
        equipHumanMain(main, owner, stage);
        applyHumanMainPose(main, stage);
        renderStand(main, 0.0D, 0.0D, 0.0D, partialTick, pose, buffers, packedLight);

        if (stage == 1) {
            ArmorStand boots = humanStands[1];
            configureArmorStand(boots, true, false, true);
            boots.setItemSlot(EquipmentSlot.FEET, redBoots());
            setLegPose(boots, 90.0F);
            prepareForRender(boots, owner, -0.4D, -0.3D, 0.0D, SOURCE_YAW, tick);
            renderStand(boots, -0.4D, -0.3D, 0.0D,
                    partialTick, pose, buffers, packedLight);
        } else if (stage == 2) {
            ArmorStand leggings = humanStands[1];
            configureArmorStand(leggings, true, false, true);
            leggings.setItemSlot(EquipmentSlot.LEGS, blackLeggings());
            setLegPose(leggings, -20.0F);
            prepareForRender(leggings, owner, 0.6D, 0.4D, 0.0D, SOURCE_YAW, tick);
            renderStand(leggings, 0.6D, 0.4D, 0.0D,
                    partialTick, pose, buffers, packedLight);

            ArmorStand boots = humanStands[2];
            configureArmorStand(boots, true, false, true);
            boots.setItemSlot(EquipmentSlot.FEET, redBoots());
            setLegPose(boots, 90.0F);
            prepareForRender(boots, owner, 0.2D, 0.2D, 0.0D, SOURCE_YAW, tick);
            renderStand(boots, 0.2D, 0.2D, 0.0D,
                    partialTick, pose, buffers, packedLight);
        } else if (stage == 3) {
            ArmorStand legsAndBoots = humanStands[1];
            configureArmorStand(legsAndBoots, true, false, true);
            legsAndBoots.setItemSlot(EquipmentSlot.FEET, redBoots());
            legsAndBoots.setItemSlot(EquipmentSlot.LEGS, blackLeggings());
            setLegPose(legsAndBoots, 90.0F);
            prepareForRender(
                    legsAndBoots, owner, 0.7D, 0.6D, 0.0D, SOURCE_YAW, tick);
            renderStand(legsAndBoots, 0.7D, 0.6D, 0.0D,
                    partialTick, pose, buffers, packedLight);
        }

        renderHumanName(owner, stage, partialTick, pose, buffers, packedLight);
    }

    void renderStatue(
            MarawTharBossEntity owner,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        ensureStands(owner.level());
        float victoryTick = owner.getVictoryTick() + partialTick;
        if (victoryTick >= STATUE_END_TICK) {
            return;
        }

        int age = Math.max(0, owner.getVictoryTick() - STATUE_SPAWN_TICK);
        float mouthOffset = Mth.lerp(partialTick, mouthOffset(age), mouthOffset(age + 1));
        int petrificationTick = Mth.floor(victoryTick) - PETRIFICATION_START_TICK;
        double shakeY = MarawTharVictoryVisualRules.shakeY(
                age, petrificationTick, partialTick);
        double shakeZ = MarawTharVictoryVisualRules.shakeZ(
                age, petrificationTick, partialTick);

        // One root transform shakes the whole statue group while every part keeps its own
        // logical coordinates and the name stand stays outside the shaken root. The pose stack
        // reaches this method already positioned at the entity with no extra yaw rotation, so
        // the translate axes match the world axes the source tp commands use.
        pose.pushPose();
        pose.translate(0.0D, shakeY, shakeZ);
        for (int index = 0; index < STATUE_PART_COUNT; index++) {
            float fallAge = victoryTick - STATUE_FALL_START_TICK - PART_FALL_DELAYS[index];
            if (fallAge >= 6.0F) {
                continue;
            }
            double fallOffset = fallAge > 0.0F
                    ? -0.5D * Math.min(6.0F, fallAge)
                    : 0.0D;

            ArmorStand part = statueParts[index];
            resetEquipment(part);
            resetPose(part);
            configureArmorStand(part, true, PART_SMALL[index], false);
            ItemStack item = statuePartItem(index, victoryTick, age, owner.getId());
            Rotations partPose = rotations(PART_POSES[index]);
            if (PART_HELD_IN_RIGHT_HAND[index]) {
                part.setRightArmPose(partPose);
                part.setItemSlot(EquipmentSlot.MAINHAND, item);
            } else {
                part.setHeadPose(partPose);
                part.setItemSlot(EquipmentSlot.HEAD, item);
            }

            double[] local = PART_LOCAL_OFFSETS[index];
            double x = local[0];
            double y = local[1] + (index <= 5 ? -mouthOffset : mouthOffset) + fallOffset;
            double z = local[2];
            prepareForRender(part, owner, x, y, z, 0.0F, owner.getVictoryTick());
            renderStand(part, x, y, z, partialTick, pose, buffers, packedLight);
        }
        pose.popPose();

        configureNameStand(owner, false);
        prepareForRender(
                nameStand, owner, 0.0D, STATUE_NAME_Y, 0.0D, 0.0F, owner.getVictoryTick());
        renderStand(nameStand, 0.0D, STATUE_NAME_Y, 0.0D,
                partialTick, pose, buffers, packedLight);
    }

    private void renderHumanName(
            MarawTharBossEntity owner,
            int stage,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        double x;
        double y;
        if (stage == 0) {
            x = 0.0D;
            y = 1.95D;
        } else if (stage == 1) {
            x = -0.1D;
            y = 2.05D;
        } else if (stage == 2) {
            x = -0.4D;
            y = 2.3D;
        } else {
            x = 0.1D;
            y = 2.7D;
        }
        configureNameStand(owner, true);
        prepareForRender(
                nameStand, owner, x, y, 0.0D, 0.0F, owner.getVictoryTick());
        renderStand(nameStand, x, y, 0.0D, partialTick, pose, buffers, packedLight);
    }

    private void configureNameStand(MarawTharBossEntity owner, boolean italic) {
        resetEquipment(nameStand);
        resetPose(nameStand);
        configureArmorStand(nameStand, true, true, false);
        Component baseName = owner.getCustomName() != null
                ? owner.getCustomName()
                : Component.translatable("entity.finalparadox.marawthar");
        nameStand.setCustomName(italic ? baseName.copy().withStyle(style -> style.withItalic(true)) : baseName);
        nameStand.setCustomNameVisible(true);
    }

    private static void equipHumanMain(
            ArmorStand stand,
            MarawTharBossEntity owner,
            int stage
    ) {
        stand.setItemSlot(EquipmentSlot.HEAD, owner.getItemBySlot(EquipmentSlot.HEAD).copy());
        stand.setItemSlot(EquipmentSlot.CHEST, blackChestplate());
        if (stage <= 1) {
            stand.setItemSlot(EquipmentSlot.LEGS, blackLeggings());
        }
        if (stage == 0) {
            stand.setItemSlot(EquipmentSlot.FEET, redBoots());
        }
    }

    private static void applyHumanMainPose(ArmorStand stand, int stage) {
        stand.setRightArmPose(new Rotations(0.0F, 0.0F, 10.0F));
        stand.setLeftArmPose(new Rotations(0.0F, 0.0F, -10.0F));
        if (stage == 0) {
            stand.setHeadPose(new Rotations(30.0F, 0.0F, 0.0F));
            return;
        }
        setLegPose(stand, -20.0F);
        if (stage == 1) {
            stand.setHeadPose(new Rotations(20.0F, 0.0F, 0.0F));
            return;
        }
        stand.setHeadPose(new Rotations(100.0F, 0.0F, 0.0F));
        stand.setBodyPose(new Rotations(stage == 2 ? 70.0F : 80.0F, 0.0F, 0.0F));
        if (stage == 3) {
            stand.setRightArmPose(new Rotations(90.0F, 330.0F, -20.0F));
            stand.setLeftArmPose(new Rotations(90.0F, 30.0F, 20.0F));
        }
    }

    private static void setLegPose(ArmorStand stand, float x) {
        Rotations pose = new Rotations(x, 0.0F, 0.0F);
        stand.setRightLegPose(pose);
        stand.setLeftLegPose(pose);
    }

    private static float mouthOffset(int age) {
        int frame = Math.floorMod(age, 20);
        if (frame >= 4 && frame <= 5) return 0.03F;
        if (frame >= 6 && frame <= 7) return 0.07F;
        if (frame >= 8 && frame <= 9) return 0.09F;
        if (frame >= 10 && frame <= 14) return 0.10F;
        if (frame >= 15 && frame <= 16) return 0.07F;
        if (frame >= 17 && frame <= 18) return 0.04F;
        if (frame >= 19) return 0.03F;
        return 0.0F;
    }

    private static ItemStack statuePartItem(
            int index,
            float victoryTick,
            int statueAge,
            int ownerId
    ) {
        int petrificationTick = Mth.floor(victoryTick) - PETRIFICATION_START_TICK;
        if (index >= 10) {
            if (petrificationTick >= 140) {
                return new ItemStack(Items.FIREWORK_STAR);
            }
            return new ItemStack(isBlinkingEye(index - 10, statueAge, ownerId)
                    ? Items.ENDER_PEARL
                    : Items.ENDER_EYE);
        }
        if (index == 5 || index == 9) {
            return new ItemStack(Items.DEAD_HORN_CORAL_FAN);
        }
        int[] blackstoneTicks = {1, 20, 40, 50, 60, -1, 70, 75, 80, -1};
        int[] stoneTicks = {83, 86, 89, 91, 93, -1, 95, 97, 99, -1};
        boolean slab = index == 0 || index == 1 || index == 4 || index == 8;
        if (stoneTicks[index] >= 0 && petrificationTick >= stoneTicks[index]) {
            return new ItemStack(slab ? Items.ANDESITE_SLAB : Items.ANDESITE_STAIRS);
        }
        if (blackstoneTicks[index] >= 0 && petrificationTick >= blackstoneTicks[index]) {
            return new ItemStack(slab ? Items.BLACKSTONE_SLAB : Items.BLACKSTONE_STAIRS);
        }
        return new ItemStack(
                slab ? Items.RED_NETHER_BRICK_SLAB : Items.RED_NETHER_BRICK_STAIRS);
    }

    private static boolean isBlinkingEye(int eye, int age, int ownerId) {
        int remaining = Math.max(0, age);
        int cycle = 0;
        while (remaining >= BLINK_INTERVALS[cycle % BLINK_INTERVALS.length]) {
            remaining -= BLINK_INTERVALS[cycle % BLINK_INTERVALS.length];
            cycle++;
        }
        if (remaining >= 2) {
            return false;
        }
        int first = Math.floorMod(ownerId + cycle * 3, 5);
        int second = Math.floorMod(first + 2 + cycle, 5);
        return eye == first || ((cycle & 1) == 0 && eye == second);
    }

    private void ensureStands(Level level) {
        if (cachedLevel == level && humanStands != null) {
            return;
        }
        cachedLevel = level;
        humanStands = new ArmorStand[HUMAN_STAND_COUNT];
        statueParts = new ArmorStand[STATUE_PART_COUNT];
        for (int index = 0; index < humanStands.length; index++) {
            humanStands[index] = new ArmorStand(level, 0.0D, 0.0D, 0.0D);
        }
        for (int index = 0; index < statueParts.length; index++) {
            statueParts[index] = new ArmorStand(level, 0.0D, 0.0D, 0.0D);
        }
        nameStand = new ArmorStand(level, 0.0D, 0.0D, 0.0D);
    }

    private static void configureArmorStand(
            ArmorStand stand,
            boolean invisible,
            boolean small,
            boolean showArms
    ) {
        CompoundTag flags = new CompoundTag();
        flags.putBoolean("Invisible", invisible);
        flags.putBoolean("Small", small);
        flags.putBoolean("ShowArms", showArms);
        flags.putBoolean("NoBasePlate", true);
        flags.putBoolean("Marker", true);
        stand.readAdditionalSaveData(flags);
        stand.setInvisible(invisible);
        stand.setShowArms(showArms);
        stand.setNoBasePlate(true);
    }

    private static void prepareForRender(
            ArmorStand stand,
            MarawTharBossEntity owner,
            double x,
            double y,
            double z,
            float yaw,
            int tick
    ) {
        stand.setPos(owner.getX() + x, owner.getY() + y, owner.getZ() + z);
        stand.setOldPosAndRot();
        stand.setYRot(yaw);
        stand.yRotO = yaw;
        stand.setYBodyRot(yaw);
        stand.yBodyRotO = yaw;
        stand.setYHeadRot(yaw);
        stand.yHeadRotO = yaw;
        stand.tickCount = tick;
    }

    private void renderStand(
            ArmorStand stand,
            double x,
            double y,
            double z,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        EntityRenderer<? super ArmorStand> renderer = dispatcher.getRenderer(stand);
        pose.pushPose();
        pose.translate(x, y, z);
        renderer.render(stand, stand.getYRot(), partialTick, pose, buffers, packedLight);
        pose.popPose();
    }

    private static void resetEquipment(ArmorStand stand) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stand.setItemSlot(slot, ItemStack.EMPTY);
        }
        stand.setCustomName(null);
        stand.setCustomNameVisible(false);
    }

    private static void resetPose(ArmorStand stand) {
        Rotations zero = new Rotations(0.0F, 0.0F, 0.0F);
        stand.setHeadPose(zero);
        stand.setBodyPose(zero);
        stand.setLeftArmPose(zero);
        stand.setRightArmPose(zero);
        stand.setLeftLegPose(zero);
        stand.setRightLegPose(zero);
    }

    private static Rotations rotations(float[] values) {
        return new Rotations(values[0], values[1], values[2]);
    }

    private static ItemStack redBoots() {
        return dyed(new ItemStack(Items.LEATHER_BOOTS), 16711680);
    }

    private static ItemStack blackLeggings() {
        return dyed(new ItemStack(Items.LEATHER_LEGGINGS), 0);
    }

    private static ItemStack blackChestplate() {
        return dyed(new ItemStack(Items.LEATHER_CHESTPLATE), 0);
    }

    private static ItemStack dyed(ItemStack stack, int color) {
        if (stack.getItem() instanceof DyeableLeatherItem leather) {
            leather.setColor(stack, color);
        }
        return stack;
    }
}
