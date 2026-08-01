package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.TharKrooBossEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Rebuilds the thirteen helmet parts summoned by the original B2
 * {@code thar_kroo/gen2.mcfunction} as one client-side composite.
 */
public final class TharKrooRenderer extends EntityRenderer<TharKrooBossEntity> {
    /*
     * DISPLAY_MODE values 0..2 are the values currently produced by the entity.
     * Values 3..6 are deliberately renderer-ready for the server animation state:
     * 3 cast-left, 4 cast-right, 5 shot, 6 finale ascension.
     *
     * The high bits may be used as a server-side sequence number when the same
     * animation is restarted. The renderer starts a local clock whenever the
     * complete state value changes, which also works for late-tracking clients.
     */
    private static final int MODE_IDLE = 0;
    private static final int MODE_HIDDEN = 1;
    private static final int MODE_FINALE = 2;
    private static final int MODE_CAST_LEFT = 3;
    private static final int MODE_CAST_RIGHT = 4;
    private static final int MODE_SHOT = 5;
    private static final int MODE_ASCENSION = 6;
    private static final int MODE_MASK = 0xFF;

    private static final ItemStack SLAB = new ItemStack(Items.POLISHED_BLACKSTONE_SLAB);
    private static final ItemStack STAIR = new ItemStack(Items.POLISHED_BLACKSTONE_STAIRS);
    private static final ItemStack CORE = new ItemStack(Items.RESPAWN_ANCHOR);
    private static final float PART_SCALE = 0.82F;

    /*
     * Exact expanded coordinates from tp_expandir.mcfunction. Index zero is
     * unused so the Java indices continue to match b2_block_id1..13.
     */
    private static final Part[] PARTS = {
            null,
            new Part(SLAB,  0.10F,  1.20F, 0.25F, 90,  90,  0, 0),
            new Part(STAIR, 0.60F,  0.50F, 0.25F,  0, 270,  0, 2),
            new Part(STAIR,-0.60F,  0.50F, 0.25F,  0,  90,  0, 2),
            new Part(SLAB,  1.40F, -0.30F, 0.25F,  0,   0,  0, 1),
            new Part(SLAB, -1.40F, -0.30F, 0.25F,  0, 180,  0, 1),
            new Part(SLAB,  1.70F,  0.00F, 0.25F,  0,   0,  0, 1),
            new Part(SLAB, -1.70F,  0.00F, 0.25F,  0, 180,  0, 1),
            new Part(CORE,  0.00F,  0.00F, 0.00F, 90,   0, 45, 0),
            new Part(STAIR, 0.50F, -0.80F, 0.25F, 90, 270,  0, 2),
            new Part(STAIR,-0.50F, -0.80F, 0.25F, 90,  90,  0, 2),
            new Part(SLAB,  0.10F, -2.30F, 0.25F, 90,  90,  0, 0),
            new Part(SLAB, -0.20F, -1.50F, 0.25F, 90,  90,  0, 1),
            new Part(SLAB,  0.40F, -1.50F, 0.25F, 90,  90,  0, 1)
    };

    /** Exact half-expanded pose from animacion_cast/pose2.mcfunction. */
    private static final Position[] CAST_CLOSED = {
            null,
            new Position( 0.10F,  0.60F, 0.125F),
            new Position( 0.30F,  0.25F, 0.125F),
            new Position(-0.30F,  0.25F, 0.125F),
            new Position( 0.70F, -0.15F, 0.125F),
            new Position(-0.70F, -0.15F, 0.125F),
            new Position( 0.85F,  0.00F, 0.125F),
            new Position(-0.85F,  0.00F, 0.125F),
            new Position( 0.00F,  0.00F, 0.000F),
            new Position( 0.25F, -0.40F, 0.125F),
            new Position(-0.25F, -0.40F, 0.125F),
            new Position( 0.10F, -1.15F, 0.125F),
            new Position(-0.20F, -0.75F, 0.125F),
            new Position( 0.40F, -0.75F, 0.125F)
    };

    /** Exact 80%-expanded pulse from animacion_cast/pose3.mcfunction. */
    private static final Position[] CAST_PULSE = {
            null,
            new Position( 0.10F,  0.96F, 0.20F),
            new Position( 0.48F,  0.40F, 0.20F),
            new Position(-0.48F,  0.40F, 0.20F),
            new Position( 1.12F, -0.30F, 0.20F),
            new Position(-1.12F, -0.30F, 0.20F),
            new Position( 1.36F,  0.00F, 0.20F),
            new Position(-1.36F,  0.00F, 0.20F),
            new Position( 0.00F,  0.00F, 0.00F),
            new Position( 0.40F, -0.64F, 0.20F),
            new Position(-0.40F, -0.64F, 0.20F),
            new Position( 0.10F, -1.84F, 0.20F),
            new Position(-0.20F, -1.20F, 0.20F),
            new Position( 0.40F, -1.20F, 0.20F)
    };

    /** Compact positions from gen2.mcfunction before tp_expandir.mcfunction runs. */
    private static final Position[] ASSEMBLY_CLOSED = {
            null,
            new Position( 0.10F,  0.70F, 0.25F),
            new Position( 0.45F,  0.30F, 0.25F),
            new Position(-0.45F,  0.30F, 0.25F),
            new Position( 0.45F, -0.30F, 0.25F),
            new Position(-0.45F, -0.30F, 0.25F),
            new Position( 0.65F,  0.00F, 0.25F),
            new Position(-0.65F,  0.00F, 0.25F),
            new Position( 0.00F,  0.00F, 0.00F),
            new Position( 0.20F, -0.50F, 0.25F),
            new Position(-0.20F, -0.50F, 0.25F),
            new Position( 0.10F, -0.70F, 0.25F),
            new Position(-0.20F, -0.50F, 0.25F),
            new Position( 0.40F, -0.50F, 0.25F)
    };

    private static final int[] ASSEMBLY_GROUP = {
            0, 1, 2, 2, 3, 3, 4, 4, 1, 5, 5, 6, 6, 6
    };

    /** Per-part starting scores from thar_kroo/end.mcfunction. */
    private static final int[] END_SCORE = {
            0, 3, 6, 6, 12, 12, 9, 9, 0, 15, 15, 21, 18, 18
    };

    /*
     * Fixed, distinct source scoreboard phases. The original randomly assigns
     * the same twelve values, then couples ids 4/6 and 5/7.
     */
    private static final int[] IDLE_PHASE = {
            0, 1, 4, 9, 13, 17, 13, 17, 0, 21, 25, 28, 32, 36
    };

    private final ItemRenderer items;
    private final Map<TharKrooBossEntity, AnimationClock> animationClocks = new WeakHashMap<>();

    public TharKrooRenderer(EntityRendererProvider.Context context) {
        super(context);
        items = context.getItemRenderer();
        // The source parts are invisible marker armor stands and cast no entity shadow.
        shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(TharKrooBossEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(TharKrooBossEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight) {
        int state = entity.displayMode();
        int mode = state & MODE_MASK;
        if (mode == MODE_HIDDEN) return;

        float animationAge = animationAge(entity, state, partialTick);
        float time = entity.tickCount + partialTick;
        float castSpin = 0.0F;
        float ascensionLift = 0.0F;

        if (mode == MODE_CAST_LEFT || mode == MODE_CAST_RIGHT) {
            float castTicks = Math.max(0.0F, Math.min(100.0F, animationAge - 5.0F));
            castSpin = castTicks * (mode == MODE_CAST_LEFT ? -0.9F : 0.9F);
        } else if (mode == MODE_ASCENSION) {
            // fase/6/run_ascension moves every component upward by 0.05 per tick.
            ascensionLift = Math.min(animationAge, 163.0F) * 0.05F;
        }

        pose.pushPose();
        // Armor-stand helmet items sit approximately 1.55 blocks above their marker origins.
        pose.translate(0.0D, 1.55D + ascensionLift, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(entity.visualYaw() + castSpin));

        for (int index = 1; index < PARTS.length; index++) {
            Part part = PARTS[index];
            Position position = positionFor(part, index, mode, animationAge);
            float bob = usesIdleBob(mode) ? idleBob(time, IDLE_PHASE[index]) : 0.0F;
            float groupSpin = ascensionSpin(part.group, mode, animationAge);
            draw(pose, buffers, entity, part, position.x, position.y + bob, position.z,
                    groupSpin, index, packedLight);
        }

        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }

    /**
     * Draws the same thirteen-part source model in B9's H6 intermission.
     * It assembles in the source's six four-tick groups, expands at tick 28,
     * and uses the original staggered teardown scores at the end.
     */
    void renderMarawTharTransition(MarawTharBossEntity entity, float entityYaw, float partialTick,
                                PoseStack pose,
                                MultiBufferSource buffers, int packedLight) {
        float appearanceAge = entity.getTransitionVisualTick()
                - MarawTharBossEntity.THAR_KROO_VISUAL_APPEAR_TICK + partialTick;
        float teardownAge = entity.getTransitionVisualTick()
                - MarawTharBossEntity.THAR_KROO_VISUAL_END_TICK + partialTick;
        boolean tearingDown =
                entity.getTransitionVisualTick() >= MarawTharBossEntity.THAR_KROO_VISUAL_END_TICK;
        float time = entity.tickCount + partialTick;

        pose.pushPose();
        pose.translate(0.0D, 1.55D, 0.0D);
        pose.mulPose(Axis.YP.rotationDegrees(entityYaw));

        for (int index = 1; index < PARTS.length; index++) {
            Part part = PARTS[index];
            Position position;
            float bob = 0.0F;

            if (tearingDown) {
                int processedTicks = Math.max(1, Mth.floor(teardownAge) + 1);
                int score = END_SCORE[index] + processedTicks;
                if (score > 27) {
                    continue;
                }
                int fallingTicks = Mth.clamp(score - 23, 0, 4);
                position = new Position(
                        part.x,
                        part.y + processedTicks * 0.05F - fallingTicks * 4.0F,
                        part.z);
            } else if (appearanceAge < 28.0F) {
                // The respawn-anchor core is equipped only when expansion completes.
                if (index == 8) {
                    continue;
                }
                Position closed = ASSEMBLY_CLOSED[index];
                float groupStart = (ASSEMBLY_GROUP[index] - 1) * 4.0F;
                position = appearanceAge >= groupStart
                        ? closed
                        : new Position(closed.x, closed.y - 8.0F, closed.z);
            } else {
                position = new Position(part.x, part.y, part.z);
                bob = idleBob(time, IDLE_PHASE[index]);
            }

            draw(pose, buffers, entity, part,
                    position.x, position.y + bob, position.z,
                    0.0F, index, packedLight);
        }

        pose.popPose();
    }

    private float animationAge(TharKrooBossEntity entity, int state, float partialTick) {
        AnimationClock clock = animationClocks.get(entity);
        if (clock == null || clock.state != state) {
            clock = new AnimationClock(state, entity.tickCount);
            animationClocks.put(entity, clock);
        }
        return Math.max(0.0F, entity.tickCount - clock.startTick + partialTick);
    }

    private static Position positionFor(Part part, int index, int mode, float animationAge) {
        if (mode == MODE_CAST_LEFT || mode == MODE_CAST_RIGHT) {
            if (animationAge < 5.0F || animationAge >= 105.0F) return CAST_CLOSED[index];
            return (((int) animationAge - 5) & 1) == 0
                    ? new Position(part.x, part.y, part.z)
                    : CAST_PULSE[index];
        }

        float x = part.x;
        float y = part.y;
        float z = part.z;
        if (mode == MODE_SHOT) {
            int tick = (int) animationAge;
            int recoil = tick < 2 ? 5 : tick <= 10 ? Math.max(0, 5 - tick / 2) : 0;
            int expansion = tick < 4 ? 0 : tick <= 14 ? Math.max(0, 7 - tick / 2) : 0;

            // Five immediate f1 calls, followed by one f2 call every two ticks.
            if (index == 2)      { x += 0.075F * recoil; y += 0.0625F * recoil; z += 0.03125F * recoil; }
            else if (index == 3) { x -= 0.075F * recoil; y += 0.0625F * recoil; z += 0.03125F * recoil; }
            else if (index == 8) { z -= 0.20F * recoil; }
            else if (index == 9) { x += 0.075F * recoil; y -= 0.10F * recoil; z += 0.03125F * recoil; }
            else if (index == 10){ x -= 0.075F * recoil; y -= 0.10F * recoil; z += 0.03125F * recoil; }

            // f3 contains five identical command blocks; f4 reverses one block every two ticks.
            if (index == 1)      { y += 0.20F * expansion; z += 0.03125F * expansion; }
            else if (index == 4) { x += 0.175F * expansion; y -= 0.0325F * expansion; z += 0.03125F * expansion; }
            else if (index == 5) { x -= 0.175F * expansion; y -= 0.0325F * expansion; z += 0.03125F * expansion; }
            else if (index == 6) { x += 0.2125F * expansion; z += 0.03125F * expansion; }
            else if (index == 7) { x -= 0.2125F * expansion; z += 0.03125F * expansion; }
            else if (index == 11){ y -= 0.15F * expansion; z += 0.03125F * expansion; }
            else if (index == 12 || index == 13) {
                y -= 0.075F * expansion;
                z += 0.03125F * expansion;
            }
        }
        return new Position(x, y, z);
    }

    private static boolean usesIdleBob(int mode) {
        return mode == MODE_IDLE || mode == MODE_SHOT || mode == MODE_FINALE || mode == MODE_ASCENSION;
    }

    /**
     * One 45-tick integration of run_stand.mcfunction's exact vertical steps,
     * centered around zero so the renderer does not slowly change its anchor.
     */
    private static float idleBob(float time, int initialScore) {
        int tick = Math.floorMod((int) Math.floor(time) + initialScore + 3, 45);
        float fraction = time - (float) Math.floor(time);
        float value = 0.0F;
        for (int i = 0; i < tick; i++) value += idleStep(i - 2);
        value += idleStep(tick - 2) * fraction;
        return value + 0.05F;
    }

    private static float idleStep(int score) {
        if (score >= -1 && score <= 0) return 0.0025F;
        if (score >= 1 && score <= 20) return -0.005F;
        if (score >= 21 && score <= 22) return -0.0025F;
        if (score >= 23 && score <= 42) return 0.005F;
        return 0.0F;
    }

    private static float ascensionSpin(int group, int mode, float animationAge) {
        if (mode != MODE_ASCENSION) return 0.0F;
        // giro2/run: main ring +8 degrees/tick, counter ring -6 degrees/tick.
        return group == 1 ? animationAge * 8.0F : group == 2 ? animationAge * -6.0F : 0.0F;
    }

    private void draw(PoseStack pose, MultiBufferSource buffers, Entity entity, Part part,
                      float x, float y, float z, float groupSpin, int index, int packedLight) {
        pose.pushPose();
        // Applying this before translation rotates both the position and the helmet pose around the core.
        if (groupSpin != 0.0F) pose.mulPose(Axis.YP.rotationDegrees(groupSpin));
        pose.translate(x, y, z);
        // ModelPart#translateAndRotate applies an armor stand head pose Z, Y, X.
        pose.mulPose(Axis.ZP.rotationDegrees(part.rz));
        pose.mulPose(Axis.YP.rotationDegrees(part.ry));
        pose.mulPose(Axis.XP.rotationDegrees(part.rx));
        pose.scale(PART_SCALE, PART_SCALE, PART_SCALE);
        items.renderStatic(part.stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                pose, buffers, entity.level(), entity.getId() + index);
        pose.popPose();
    }

    private record Position(float x, float y, float z) {
    }

    private record Part(ItemStack stack, float x, float y, float z,
                        float rx, float ry, float rz, int group) {
    }

    private record AnimationClock(int state, int startTick) {
    }
}
