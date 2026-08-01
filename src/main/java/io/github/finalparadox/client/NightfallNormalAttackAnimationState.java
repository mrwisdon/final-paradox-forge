package io.github.finalparadox.client;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.ability.NightfallAbilityState;
import io.github.finalparadox.ability.NightfallSlashFrames;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client-owned visual sequencing for Nightfall's three ordinary attack clips.
 * Every accepted click still performs exactly one vanilla attack. The strike
 * portion cannot be interrupted, while the recovery can flow directly into
 * the next clip instead of forcing the sword through its idle pose.
 */
@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, value = Dist.CLIENT)
public final class NightfallNormalAttackAnimationState {
    static final int ANIMATION_TICKS = 12;
    private static final int[] CHAIN_START_TICKS = {8, 8, 10};
    private static final int SEQUENCE_RESET_TICKS = 30;
    private static final int FIRST_PARTICLE_TICK = 5;
    private static final int PARTICLE_FRAME_COUNT = 3;
    private static final int PARTICLE_SAMPLE_STRIDE = 8;
    private static final double[] TRAIL_DISTANCES =
            {0.9D, 1.1D, 1.3D, 1.5D, 1.7D, 1.9D, 2.1D, 2.3D, 2.5D, 2.7D, 2.9D};
    private static final DustParticleOptions[] TRAIL_DUST = {
            dust(0.0F, 0.0F, 0.0F, 0.20F),
            dust(0.0F, 0.0F, 0.0F, 0.25F),
            dust(0.0F, 0.0F, 0.0F, 0.30F),
            dust(0.0F, 0.0F, 0.0F, 0.35F),
            dust(0.0F, 0.0F, 0.0F, 0.40F),
            dust(0.0F, 0.0F, 0.0F, 0.45F),
            dust(0.173F, 0.129F, 0.129F, 0.50F),
            dust(0.267F, 0.169F, 0.169F, 0.55F),
            dust(0.478F, 0.188F, 0.188F, 0.60F),
            dust(0.694F, 0.157F, 0.157F, 0.65F),
            dust(1.0F, 0.0F, 0.0F, 0.70F)
    };

    private static final Map<UUID, State> STATES = new HashMap<>();
    private static ClientLevel trackedLevel;

    private NightfallNormalAttackAnimationState() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() && !event.isUseItem()) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.NIGHTFALL.get())) return;

        if (event.isUseItem() && isActive(player)) {
            event.setSwingHand(false);
            event.setCanceled(true);
            return;
        }
        if (!event.isAttack()) return;
        if (isComboAnimating(stack)
                || !startAttack(player, hand, minecraft.level.getGameTime())) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            STATES.clear();
            trackedLevel = null;
            return;
        }
        if (level != trackedLevel) {
            STATES.clear();
            trackedLevel = level;
        }

        long gameTime = level.getGameTime();
        Set<UUID> presentPlayers = level.players().stream()
                .map(Player::getUUID)
                .collect(Collectors.toSet());
        STATES.keySet().removeIf(id -> !presentPlayers.contains(id));
        for (Player player : level.players()) {
            tickPlayer(level, player, gameTime);
        }
    }

    static int attackIndex(Player player, InteractionHand hand) {
        State state = STATES.get(player.getUUID());
        return state != null && state.activeHand == hand ? state.activeAttack : -1;
    }

    static int chainFromAttackIndex(Player player, InteractionHand hand) {
        State state = STATES.get(player.getUUID());
        return state != null && state.activeHand == hand ? state.chainFromAttack : -1;
    }

    static float chainFromProgress(Player player, InteractionHand hand) {
        State state = STATES.get(player.getUUID());
        return state != null && state.activeHand == hand ? state.chainFromProgress : -1.0F;
    }

    static float progress(Player player, InteractionHand hand, float partialTick) {
        State state = STATES.get(player.getUUID());
        if (state == null || state.activeAttack < 0 || state.activeHand != hand) {
            return -1.0F;
        }
        float elapsed = player.level().getGameTime() - state.startTick + partialTick;
        if (elapsed < 0.0F || elapsed >= ANIMATION_TICKS) return -1.0F;
        return Mth.clamp(elapsed / ANIMATION_TICKS, 0.0F, 1.0F);
    }

    static boolean isActive(Player player) {
        State state = STATES.get(player.getUUID());
        return state != null
                && state.activeAttack >= 0
                && player.level().getGameTime() - state.startTick < ANIMATION_TICKS;
    }

    private static boolean startAttack(Player player, InteractionHand hand, long gameTime) {
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        int chainFromAttack = -1;
        float chainFromProgress = -1.0F;
        if (state.activeAttack >= 0) {
            long elapsed = gameTime - state.startTick;
            if (elapsed < CHAIN_START_TICKS[state.activeAttack]) return false;
            if (elapsed < ANIMATION_TICKS) {
                emitPendingSlashTrail(player, state, elapsed);
                chainFromAttack = state.activeAttack;
                chainFromProgress = Mth.clamp(
                        elapsed / (float) ANIMATION_TICKS, 0.0F, 1.0F);
            } else {
                state.finish(gameTime);
            }
        }
        if (state.lastFinishedTick != Long.MIN_VALUE
                && gameTime - state.lastFinishedTick > SEQUENCE_RESET_TICKS) {
            state.nextAttack = 0;
        }
        state.activeAttack = state.nextAttack;
        state.nextAttack = (state.nextAttack + 1) % NightfallNormalAttackFrames.ATTACK_COUNT;
        state.activeHand = hand;
        state.startTick = gameTime;
        state.nextParticleFrame = 0;
        state.chainFromAttack = chainFromAttack;
        state.chainFromProgress = chainFromProgress;
        state.lastFinishedTick = Long.MIN_VALUE;
        return true;
    }

    private static void tickPlayer(ClientLevel level, Player player, long gameTime) {
        State state = STATES.computeIfAbsent(player.getUUID(), ignored -> new State());
        float observedAttack = player.getAttackAnim(1.0F);
        boolean swingStarted = observedAttack > 0.001F
                && (state.lastObservedAttack <= 0.001F
                || observedAttack + 0.05F < state.lastObservedAttack);
        state.lastObservedAttack = observedAttack;

        if (swingStarted && player.swingingArm != null) {
            InteractionHand hand = player.swingingArm;
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(ModItems.NIGHTFALL.get()) && !isComboAnimating(stack)) {
                startAttack(player, hand, gameTime);
            }
        }

        if (state.activeAttack < 0) return;
        ItemStack activeStack = player.getItemInHand(state.activeHand);
        if (!activeStack.is(ModItems.NIGHTFALL.get()) || isComboAnimating(activeStack)) {
            state.cancel();
            return;
        }

        long elapsed = gameTime - state.startTick;
        while (state.nextParticleFrame < PARTICLE_FRAME_COUNT
                && elapsed >= FIRST_PARTICLE_TICK + state.nextParticleFrame) {
            emitSlashTrail(level, player, state.activeHand,
                    state.activeAttack, state.nextParticleFrame);
            state.nextParticleFrame++;
        }
        if (elapsed >= ANIMATION_TICKS) state.finish(gameTime);
    }

    private static void emitPendingSlashTrail(Player player, State state, long elapsed) {
        if (!(player.level() instanceof ClientLevel level)) return;
        while (state.nextParticleFrame < PARTICLE_FRAME_COUNT
                && elapsed >= FIRST_PARTICLE_TICK + state.nextParticleFrame) {
            emitSlashTrail(level, player, state.activeHand,
                    state.activeAttack, state.nextParticleFrame);
            state.nextParticleFrame++;
        }
    }

    private static void emitSlashTrail(ClientLevel level, Player player, InteractionHand hand,
                                       int attackIndex, int particleFrame) {
        int sourceFrame = attackIndex * PARTICLE_FRAME_COUNT + particleFrame;
        NightfallSlashFrames.Sample[] samples = NightfallSlashFrames.FRAMES[sourceFrame];
        float baseYaw = Mth.rotLerp(1.0F, player.yBodyRotO, player.yBodyRot);
        Vec3 forward = Vec3.directionFromRotation(0.0F, baseYaw);
        Vec3 left = forward.yRot((float) (Math.PI / 2.0D));
        float side = hand == InteractionHand.OFF_HAND ? -1.0F : 1.0F;
        Vec3 origin = player.position();

        for (int sampleIndex = 0; sampleIndex < samples.length;
             sampleIndex += PARTICLE_SAMPLE_STRIDE) {
            emitSample(level, samples[sampleIndex], origin, forward, left, baseYaw, side);
        }
        int finalIndex = samples.length - 1;
        if (finalIndex % PARTICLE_SAMPLE_STRIDE != 0) {
            emitSample(level, samples[finalIndex], origin, forward, left, baseYaw, side);
        }
    }

    private static void emitSample(ClientLevel level, NightfallSlashFrames.Sample sample,
                                   Vec3 origin, Vec3 forward, Vec3 left,
                                   float baseYaw, float side) {
        Vec3 base = origin
                .add(left.scale(sample.left() * side))
                .add(0.0D, sample.up(), 0.0D)
                .add(forward.scale(sample.forward()));
        Vec3 ray = Vec3.directionFromRotation(
                sample.pitch(), baseYaw + sample.yaw() * side);
        for (int index = 0; index < TRAIL_DISTANCES.length; index++) {
            Vec3 point = base.add(ray.scale(TRAIL_DISTANCES[index]));
            level.addParticle(TRAIL_DUST[index],
                    point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
        }
        Vec3 tip = base.add(ray.scale(3.0D));
        level.addParticle(ParticleTypes.END_ROD,
                tip.x, tip.y, tip.z, 0.0D, 0.0D, 0.0D);
    }

    private static boolean isComboAnimating(ItemStack stack) {
        return stack.hasTag()
                && stack.getTag().contains(NightfallAbilityState.COMBO_ANIMATION_SCORE_TAG);
    }

    private static DustParticleOptions dust(float red, float green, float blue, float size) {
        return new DustParticleOptions(new Vector3f(red, green, blue), size);
    }

    private static final class State {
        private float lastObservedAttack;
        private long startTick = Long.MIN_VALUE;
        private long lastFinishedTick = Long.MIN_VALUE;
        private int activeAttack = -1;
        private int nextAttack;
        private InteractionHand activeHand = InteractionHand.MAIN_HAND;
        private int nextParticleFrame;
        private int chainFromAttack = -1;
        private float chainFromProgress = -1.0F;

        private void finish(long gameTime) {
            activeAttack = -1;
            startTick = Long.MIN_VALUE;
            lastFinishedTick = gameTime;
            nextParticleFrame = PARTICLE_FRAME_COUNT;
            chainFromAttack = -1;
            chainFromProgress = -1.0F;
        }

        private void cancel() {
            activeAttack = -1;
            nextAttack = 0;
            startTick = Long.MIN_VALUE;
            lastFinishedTick = Long.MIN_VALUE;
            nextParticleFrame = PARTICLE_FRAME_COUNT;
            chainFromAttack = -1;
            chainFromProgress = -1.0F;
        }
    }
}
